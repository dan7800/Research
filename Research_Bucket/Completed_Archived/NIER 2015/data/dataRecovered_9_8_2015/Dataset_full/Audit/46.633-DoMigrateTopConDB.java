package com.vlee.servlet.test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vlee.ejb.accounting.BranchNut;
import com.vlee.ejb.accounting.BranchObject;
import com.vlee.ejb.accounting.CashAccTxnBean;
import com.vlee.ejb.accounting.CashAccTxnNut;
import com.vlee.ejb.accounting.CashAccTxnObject;
import com.vlee.ejb.accounting.CashAccountNut;
import com.vlee.ejb.accounting.CashAccountObject;
import com.vlee.ejb.accounting.GLCodeBean;
import com.vlee.ejb.accounting.GenericEntityAccountBean;
import com.vlee.ejb.accounting.GenericStmt;
import com.vlee.ejb.accounting.GenericStmtBean;
import com.vlee.ejb.accounting.GenericStmtNut;
import com.vlee.ejb.accounting.GenericStmtObject;
import com.vlee.ejb.accounting.NominalAccount;
import com.vlee.ejb.accounting.NominalAccountBean;
import com.vlee.ejb.accounting.NominalAccountNut;
import com.vlee.ejb.accounting.NominalAccountObject;
import com.vlee.ejb.accounting.NominalAccountTxn;
import com.vlee.ejb.accounting.NominalAccountTxnBean;
import com.vlee.ejb.accounting.NominalAccountTxnNut;
import com.vlee.ejb.accounting.NominalAccountTxnObject;
import com.vlee.ejb.accounting.OfficialReceipt;
import com.vlee.ejb.accounting.OfficialReceiptNut;
import com.vlee.ejb.accounting.OfficialReceiptObject;
import com.vlee.ejb.customer.CustAccount;
import com.vlee.ejb.customer.CustAccountBean;
import com.vlee.ejb.customer.CustAccountNut;
import com.vlee.ejb.customer.CustServiceCenterNut;
import com.vlee.ejb.customer.CustServiceCenterObject;
import com.vlee.ejb.customer.Invoice;
import com.vlee.ejb.customer.InvoiceBean;
import com.vlee.ejb.customer.InvoiceItem;
import com.vlee.ejb.customer.InvoiceItemNut;
import com.vlee.ejb.customer.InvoiceItemObject;
import com.vlee.ejb.customer.InvoiceNut;
import com.vlee.ejb.customer.InvoiceObject;
import com.vlee.ejb.inventory.Item;
import com.vlee.ejb.inventory.ItemBean;
import com.vlee.ejb.inventory.ItemNut;
import com.vlee.ejb.inventory.ItemObject;
import com.vlee.ejb.inventory.Location;
import com.vlee.ejb.inventory.LocationNut;
import com.vlee.ejb.inventory.Stock;
import com.vlee.ejb.inventory.StockNut;
import com.vlee.ejb.inventory.StockObject;
import com.vlee.ejb.user.RoleBean;
import com.vlee.ejb.user.User;
import com.vlee.ejb.user.UserDetails;
import com.vlee.ejb.user.UserDetailsNut;
import com.vlee.ejb.user.UserNut;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionDo;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.CurrencyFormat;
import com.vlee.util.Log;
import com.vlee.util.Task;
import com.vlee.util.TimeFormat;

public class DoMigrateTopConDB implements Action
{
	private String strClassName = "DoMigrateTopConDB";
	private static Task curTask = null;
	// Constants
	private static Integer iDefLocId = new Integer(1000);
	private static Integer iDefSvcCtrId = new Integer(1);
	private static Integer iDefPCCenterId = new Integer(1000);
	private static Integer iDefCondId = new Integer(StockNut.STK_COND_GOOD);
	private static Integer iDefCashAccId = new Integer(1000);
	private static Integer iDefChequeAccId = new Integer(1001);
	private BranchObject defBranchObj = null;
	private static HashMap hmCurr = new HashMap();
	static
	{
		hmCurr.put(new Integer(1), "MYR");
		hmCurr.put(new Integer(2), "USD");
		hmCurr.put(new Integer(3), "SGD");
		hmCurr.put(new Integer(4), "4");
		hmCurr.put(new Integer(5), "5");
		hmCurr.put(new Integer(6), "6");
		hmCurr.put(new Integer(7), "7");
	}

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
			/*******************************************************************
			 * // 1. Extract BranchInfo
			 ******************************************************************/
//			String query = "select * from branchinfo";
//			Statement stmt = con.createStatement();
//			ResultSet rs = stmt.executeQuery(query);
//			curTask = new Task("Extract BranchInfo", rs.getFetchSize());
//			while (rs.next())
//			{
//				curTask.increment();
//				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
//				Log.printVerbose("Got one ... ");
//				String coNo = rs.getString("cono");
//				String branchName = rs.getString("branchname");
//				String coname = rs.getString("coname");
//				Log.printVerbose("coNo = " + coNo);
//				Log.printVerbose("branchName = " + branchName);
//				Log.printVerbose("coname = " + coname);
//				// insert to location, cust_svc_center, supp_svc_center
//				// Assumes locaddr has a default row of pkid = 0
//				// NEW: Discovered that they are 2 Locid ("00","99")
//				Location newLoc = LocationNut.getObjectByCode("00");
//				if (newLoc == null)
//				{
//					newLoc = LocationNut.getHome().create(LocationBean.NS_TRADING, "", "00", branchName + " (00)",
//							coname, iDefPCCenterId, new Integer(0), TimeFormat.getTimestamp(), usrid);
//				}
//				Location newLoc2 = LocationNut.getObjectByCode("99");
//				if (newLoc2 == null)
//				{
//					newLoc2 = LocationNut.getHome().create(LocationBean.NS_TRADING, "", "99", branchName + " (99)",
//							coname, iDefPCCenterId, new Integer(0), TimeFormat.getTimestamp(), usrid);
//				}
//				Branch newBranch = BranchNut.getObjectByCode(coNo);
//				if (newBranch == null)
//				{
//					BranchObject newBranchObj = new BranchObject();
//					newBranchObj.code = coNo;
//					// newBranchObj.regNo = "";
//					newBranchObj.name = branchName;
//					newBranchObj.description = coname;
//					// newBranchObj.addr1 = req.getParameter("addr1");
//					// newBranchObj.addr2 = req.getParameter("addr2");
//					// newBranchObj.addr3 = req.getParameter("addr3");
//					// newBranchObj.zip = req.getParameter("zip");
//					// newBranchObj.state = req.getParameter("state");
//					// newBranchObj.countryCode =
//					// req.getParameter("countryCode");
//					// newBranchObj.phoneNo = req.getParameter("phoneNo");
//					// newBranchObj.faxNo = req.getParameter("faxNo");
//					// newBranchObj.webUrl = req.getParameter("webUrl");
//					newBranchObj.accPCCenterId = iDefPCCenterId;
//					newBranchObj.invLocationId = iDefLocId;
//					// newBranchObj.cashbookCash = new
//					// Integer(req.getParameter("cashbookCash"));
//					// newBranchObj.cashbookCard = new
//					// Integer(req.getParameter("cashbookCard"));
//					// newBranchObj.cashbookCheque = new
//					// Integer(req.getParameter("cashbookCheque"));
//					// newBranchObj.cashbookPDCheque = new
//					// Integer(req.getParameter("cashbookPDCheque"));
//					// newBranchObj.currency = req.getParameter("currency");
//					// newBranchObj.pricing = req.getParameter("pricing");
//					// newBranchObj.hotlines = req.getParameter("hotlines");
//					// newBranchObj.logoURL= req.getParameter("logoURL");
//					newBranch = BranchNut.fnCreate(newBranchObj);
//				}
//			}
//			stmt.close();
			/*******************************************************************
			 * // 2. Extract Customer Info
			 ******************************************************************/
//			String custQuery = "select * from cus";
//			Statement custStmt = con.createStatement();
//			ResultSet rsCust = custStmt.executeQuery(custQuery);
//			// String cleanCust = "delete from cust_account_index where pkid !=
//			// 1";
//			// Statement cleanStmt = jbossCon.createStatement();
//			curTask = new Task("Extract Customer", rsCust.getFetchSize());
//			while (rsCust.next())
//			{
//				curTask.increment();
//				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
//				Log.printVerbose("Got a customer ... ");
//				String custId = rsCust.getString("custid");
//				String shortName = rsCust.getString("shortname");
//				String name1 = rsCust.getString("name1");
//				String custType = rsCust.getString("custtype");
//				String contact = rsCust.getString("contact");
//				String addr1 = rsCust.getString("addr1");
//				String addr2 = rsCust.getString("addr2");
//				String addr3 = rsCust.getString("addr3");
//				String tel = rsCust.getString("tel");
//				String fax = rsCust.getString("fax");
//				// Create the custAccount
//				CustAccount newCustAcc = CustAccountNut.getObjectByCode(custId);
//				if (newCustAcc == null)
//				{
//					CustAccountObject custObj = new CustAccountObject();
//					custObj.name = shortName;
//					custObj.custAccountCode = custId;
//					custObj.description = name1;
//					custObj.nameFirst = contact;
//					// custObj.accType = CustAccountBean.ACCTYPE_NORMAL_ENUM;
//					custObj.mainAddress1 = addr1;
//					custObj.mainAddress2 = addr2;
//					custObj.mainAddress3 = addr3;
//					custObj.telephone1 = tel;
//					custObj.faxNo = fax;
//					newCustAcc = CustAccountNut.fnCreate(custObj);
//					if (newCustAcc == null)
//						throw new Exception("Failed to create CustAccount " + custId);
//				}
//			}
//			custStmt.close();
			/*******************************************************************
			 * // 2a. Extract SalesRep Info
			 ******************************************************************/
//			String salesRepQuery = "select * from srep";
//			Statement salesRepStmt = con.createStatement();
//			ResultSet rsSalesRep = salesRepStmt.executeQuery(salesRepQuery);
//			curTask = new Task("Extract SalesRep", rsSalesRep.getFetchSize());
//			while (rsSalesRep.next())
//			{
//				curTask.increment();
//				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
//				String srId = rsSalesRep.getString("srid");
//				String srDesc = rsSalesRep.getString("description");
//				createNewSRep(srId, srDesc);
//			} // end rsSalesRep
			/*******************************************************************
			 * // 3. Extract Stock Info
			 ******************************************************************/
//			// Create a DUMMY Non-Inventory Item to track all stocks that have
//			// missing or unaccounted stkId
//			Item nonInvItem = ItemNut.getObjectByCode("non-inv");
//			if (nonInvItem == null)
//			{
//				ItemObject itemObj = new ItemObject();
//				// populate the properties here!!
//				itemObj.code = "non-inv";
//				itemObj.name = "Non-Inventory";
//				itemObj.description = "General Non-Inventory";
//				itemObj.userIdUpdate = usrid;
//				itemObj.enumInvType = new Integer(ItemBean.INV_TYPE_NONSTK);
//				nonInvItem = ItemNut.fnCreate(itemObj);
//			}
//			// Create a corresponding NON_INV PosItem
//			POSItem nonInvPOSItem = POSItemNut.getPOSItem(nonInvItem.getPkid(), POSItemBean.TYPE_NINV);
//			if (nonInvPOSItem == null)
//			{
//				POSItemObject newNIPosItemObj = new POSItemObject();
//				newNIPosItemObj.itemFKId = nonInvItem.getPkid();
//				newNIPosItemObj.itemType = POSItemBean.TYPE_NINV;
//				newNIPosItemObj.currency = "MYR";
//				// newNIPosItemObj.unitPriceStd = new BigDecimal("0.00");
//				// newNIPosItemObj.unitPriceDiscounted = new BigDecimal("0.00");
//				// newNIPosItemObj.unitPriceMin = new BigDecimal("0.00");
//				// newNIPosItemObj.timeEffective = TimeFormat.getTimestamp();
//				newNIPosItemObj.status = POSItemBean.STATUS_ACTIVE;
//				// newNIPosItemObj.lastUpdate = TimeFormat.getTimestamp();
//				newNIPosItemObj.userIdUpdate = usrid;
//				// newNIPosItemObj.costOfItem = new BigDecimal("0.00");
//				nonInvPOSItem = POSItemNut.fnCreate(newNIPosItemObj);
//			}
//			String stkQuery = "select * from stkmaster";
//			Statement stkStmt = con.createStatement();
//			ResultSet rsStk = stkStmt.executeQuery(stkQuery);
//			// String cleanCust = "delete from cust_account_index where pkid !=
//			// 1";
//			// Statement cleanStmt = jbossCon.createStatement();
//			curTask = new Task("Extract Stock", rsStk.getFetchSize());
//			while (rsStk.next())
//			{
//				curTask.increment();
//				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
//				String stkId = rsStk.getString("stkid");
//				String shortName = rsStk.getString("shortname");
//				String desc1 = rsStk.getString("desc1");
//				String desc2 = rsStk.getString("desc2");
//				String desc3 = rsStk.getString("desc3");
//				BigDecimal aveUCost = rsStk.getBigDecimal("aveucost");
//				BigDecimal rplUCost = rsStk.getBigDecimal("rplucost");
//				BigDecimal curBalQty = rsStk.getBigDecimal("curbalqty");
//				String catid = rsStk.getString("catid");// / map to category1 of
//														// inv_item
//				String deptid = rsStk.getString("deptid");// / map to
//															// category2 of
//															// inv_item
//				String grpid = rsStk.getString("grpid");// / map to category3 of
//														// inv_item
//				// Add the item index if doesn't already exist
//				Item newItem = ItemNut.getObjectByCode(stkId);
//				if (newItem == null)
//				{
//					String uomQuery = "select distinct uom from cdhisd where stkid = '" + stkId + "'";
//					Statement uomStmt = con.createStatement();
//					ResultSet rsUOM = uomStmt.executeQuery(uomQuery);
//					Integer thisUOM = new Integer(ItemBean.UOM_NONE);
//					ItemObject itemObj = new ItemObject();
//					if (rsUOM.next())
//					{
//						String uom = rsUOM.getString("uom");
//						itemObj.uom = uom;
//						if (uom.equals("PCE") || uom.equals("PCS"))
//							thisUOM = new Integer(ItemBean.UOM_PCS);
//						else if (uom.equals("ROLL"))
//							thisUOM = new Integer(ItemBean.UOM_ROLL);
//						else if (uom.equals("SET") || uom.equals("SETS") || uom.equals("SSET"))
//							thisUOM = new Integer(ItemBean.UOM_SET);
//						else if (uom.equals("UNIT") || uom.equals("NOS"))
//							thisUOM = new Integer(ItemBean.UOM_UNIT);
//					}
//					// populate the properties here!!
//					itemObj.code = stkId;
//					itemObj.name = shortName;
//					itemObj.description = desc1 + desc2 + desc3;
//					itemObj.userIdUpdate = usrid;
//					itemObj.enumInvType = new Integer(ItemBean.INV_TYPE_INVENTORY);
//					itemObj.categoryId = new Integer(1000);
//					itemObj.unitOfMeasure = thisUOM;
//					itemObj.category1 = catid;
//					itemObj.category2 = deptid;
//					itemObj.category3 = grpid;
//					newItem = ItemNut.fnCreate(itemObj);
//				}
//				// Then populate the POSITems
//				POSItem newPOSItem = POSItemNut.getPOSItem(newItem.getPkid(), POSItemBean.TYPE_INV);
//				if (newPOSItem == null)
//				{
//					POSItemObject newPOSItemObj = new POSItemObject();
//					newPOSItemObj.itemFKId = newItem.getPkid();
//					newPOSItemObj.itemType = POSItemBean.TYPE_INV;
//					newPOSItemObj.currency = "MYR";
//					newPOSItemObj.unitPriceStd = rplUCost;
//					// newPOSItemObj.unitPriceDiscounted = new
//					// BigDecimal("0.00");
//					// newPOSItemObj.unitPriceMin = new BigDecimal("0.00");
//					// newPOSItemObj.timeEffective = TimeFormat.getTimestamp();
//					newPOSItemObj.status = POSItemBean.STATUS_ACTIVE;
//					// newPOSItemObj.lastUpdate = TimeFormat.getTimestamp();
//					newPOSItemObj.userIdUpdate = usrid;
//					// newPOSItemObj.costOfItem = new BigDecimal("0.00");
//					newPOSItem = POSItemNut.fnCreate(newPOSItemObj);
//				}
//				// Then populate the corresponding stock table (only 1 row for
//				// each item since we only have one warehouse
//				Stock newStk = StockNut.getObjectBy(newItem.getPkid(), iDefLocId, iDefCondId, "");
//				if (newStk == null)
//				{
//					StockObject newObj = new StockObject();
//					newObj.itemId = newItem.getPkid();
//					newObj.locationId = iDefLocId;
//					newObj.accPCCenterId = iDefPCCenterId;
//					newObj.userIdUpdate = usrid;
//					newStk = StockNut.fnCreate(newObj);
//				}
//			}
//			stkStmt.close();
			/*******************************************************************
			 * // 4. Extract Sales Txn
			 ******************************************************************/
			/*
			 * a. Sort the CDHis by TxDate b. For each row, switch(txType) if
			 * (I) // for invoice Create a SalesTxn from the top down to Nominal
			 * Account, in this manner: SalesOrder -> SOItems -> DO -> DOItems ->
			 * Invoice -> InvoiceItems -> Nominal Account
			 * 
			 * The soItems/invoiceItems taken from CDHisD table (foreign key =
			 * DocRef)
			 * 
			 * if (P) // for Payment Find each invoice mapped to the payment
			 * (foreign key = docRef) If > 1 mapping, i.e. payment is used to
			 * pay > 1 invoice, put that info in the remarks of the receipt for
			 * the time being, If 1 mapping, put that into SalesTxnId (TO_DO:
			 * Waiting for the acc_settlement table to be out) Update the
			 * NominalAccount accordingly (should be done inside Receipt)
			 * 
			 * if (C) // Credit Note Create new Credit Note (using Generic Stmt)
			 * 
			 * if (D) // Debit Note Create new Debit Note (using Generic Stmt) //
			 * may not be ready yet, but leave this out first
			 * 
			 * if (R) // Reverse Payment, used to bring the Invoice back to its
			 * original state, // e.g. "Acct Closed", "Credit Card Refund
			 * Reversed or", "Nsf", "Payment Reversal", "Resubmit Check",
			 * "Uncollectable", "Wrong Amount", "Wrong Customer", "Wrong
			 * Invoice" // Reverse Payment debits the Customer's Account which
			 * means, nullifies earlier payments.
			 * 
			 */
			Integer offset = null;
			Integer limit = null;
			if (req.getParameter("all") != null)
			{
				offset = new Integer(0);
			} else
			{
				try
				{
					offset = new Integer(req.getParameter("offset"));
				} catch (Exception ex)
				{
					offset = new Integer(0);
				}
				try
				{
					limit = new Integer(req.getParameter("limit"));
				} catch (Exception ex)
				{
				}
				;
			}
			// Integer limit = new Integer(100);
			// Read the CDHis table ...
			// String custTxnQuery = "select * from cdhis order by txdate";
			String custTxnQuery = "select * from cdhis ";
			// custTxnQuery += " where txstatus != 'D'"; // Ignore deleted docs
			// custTxnQuery += " where custid = 'SC0001' and (txtype = 'P' or
			// txtype = 'C')"; // Don't take Loan Account
			// custTxnQuery += " where custid in (select distinct custid from
			// cdhis where currid != 1) ";
			custTxnQuery += " where custid != 'AA0001'"; // Don't take Loan
															// Account
			custTxnQuery += " and txstatus != 'H'"; // Ignore invoices on hold
			// custTxnQuery += " where txtype = 'P'"; // Don't take Loan Account
			custTxnQuery += " order by txdate ";
			if (offset != null)
				custTxnQuery += " offset " + offset.intValue();
			if (limit != null)
				custTxnQuery += " limit " + limit.intValue();
			
			Log.printVerbose(custTxnQuery);
			// String custTxnQuery = "select * from cdhis where txtype = 'I'
			// order by txdate offset 10000 limit 100";
			// String custTxnQuery = "select * from cdhis where txtype = 'I'
			// order by txdate limit 10000";
			Statement custTxnStmt = con.createStatement();
			ResultSet rsCustTxn = custTxnStmt.executeQuery(custTxnQuery);
			/*
			 * // Clear the existing tables String cleanCashAccTxn = "delete
			 * from acc_cash_transactions"; String cleanNomAccTxn = "delete from
			 * acc_nominal_account_txn"; String cleanNomAcc = "delete from
			 * acc_nominal_account"; String cleanDocLink = "delete from
			 * acc_doclink"; String cleanGenStmt = "delete from
			 * acc_generic_stmt"; String cleanReceipt = "delete from
			 * cust_receipt_index"; String cleanInvoiceItem = "delete from
			 * cust_invoice_item"; String cleanDOItem = "delete from
			 * cust_delivery_order_item"; String cleanJobsheetItem = "delete
			 * from cust_jobsheet_item"; String cleanInvoice = "delete from
			 * cust_invoice_index"; String cleanDO = "delete from
			 * cust_delivery_order_index"; String cleanJobsheet = "delete from
			 * cust_jobsheet_index"; String cleanSalesTxn = "delete from
			 * cust_sales_txn_index"; String cleanTableCounter = "delete from
			 * app_table_counter" + " where tablename ~ 'jobsheet'" + " or
			 * tablename ~ 'delivery_order'" + " or tablename ~ 'acc_doclink'" + "
			 * or tablename ~ 'invoice'"; Statement cleanStmt =
			 * jbossCon.createStatement();
			 * cleanStmt.executeUpdate(cleanCashAccTxn);
			 * cleanStmt.executeUpdate(cleanNomAccTxn);
			 * cleanStmt.executeUpdate(cleanNomAcc);
			 * cleanStmt.executeUpdate(cleanDocLink);
			 * cleanStmt.executeUpdate(cleanGenStmt);
			 * cleanStmt.executeUpdate(cleanReceipt);
			 * cleanStmt.executeUpdate(cleanInvoiceItem);
			 * cleanStmt.executeUpdate(cleanDOItem);
			 * cleanStmt.executeUpdate(cleanJobsheetItem);
			 * cleanStmt.executeUpdate(cleanInvoice);
			 * cleanStmt.executeUpdate(cleanDO);
			 * cleanStmt.executeUpdate(cleanJobsheet);
			 * cleanStmt.executeUpdate(cleanSalesTxn);
			 * cleanStmt.executeUpdate(cleanTableCounter); cleanStmt.close();
			 */
			// int count = 0;
			curTask = new Task("Extract SalesTxns", rsCustTxn.getFetchSize());
			int count = offset.intValue();
			while (rsCustTxn.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				// Log.printVerbose("Processing Txn " + count);
				Log.printDebug("Processing Txn " + count);
				count++;
				// Get the txType
				String txType = rsCustTxn.getString("txtype");
				String docNo = rsCustTxn.getString("docno");
				String docRef = rsCustTxn.getString("docRef");
				String docInfo = rsCustTxn.getString("docInfo");
				String salesRepId = rsCustTxn.getString("srid");
				String custId = rsCustTxn.getString("custid");
				String currId = rsCustTxn.getString("currid");
				BigDecimal exchRate = rsCustTxn.getBigDecimal("exch_rate");
				Timestamp txDate = rsCustTxn.getTimestamp("txdate");
				BigDecimal term = rsCustTxn.getBigDecimal("term");
				BigDecimal txAmt = rsCustTxn.getBigDecimal("txamt");
				BigDecimal lTxAmt = rsCustTxn.getBigDecimal("ltxamt");
				// BigDecimal bfAmt = rsCustTxn.getBigDecimal("bfamt");
				// BigDecimal lBfAmt = rsCustTxn.getBigDecimal("lbfamt");
				BigDecimal debitAmt = rsCustTxn.getBigDecimal("debitamt");
				BigDecimal lDebitAmt = rsCustTxn.getBigDecimal("ldebitamt");
				BigDecimal creditAmt = rsCustTxn.getBigDecimal("creditamt");
				BigDecimal lCreditAmt = rsCustTxn.getBigDecimal("lcreditamt");
				String postDate = rsCustTxn.getString("postdate");
				String comment = rsCustTxn.getString("comment");
				String txStatus = rsCustTxn.getString("txstatus");
				String batchNo = rsCustTxn.getString("batchno");
				String itemNo = rsCustTxn.getString("itemno");
				BigDecimal netSales = rsCustTxn.getBigDecimal("netsales");
				BigDecimal lNetSales = rsCustTxn.getBigDecimal("lnetsales");
				BigDecimal lTotalCost = rsCustTxn.getBigDecimal("ltotalcost");
				String ageingDate = rsCustTxn.getString("ageingdate");
				String lIntDate = rsCustTxn.getString("lintdate");
				// Check if Customer exist
				CustAccount thisCustAcc = CustAccountNut.getObjectByCode(custId);
				if (thisCustAcc == null)
				{
					Log.printDebug(count + ": Cust Code = " + custId
							+ " doesn't exist in StkMaster, creating a temp one!!");
					// continue;
					thisCustAcc = CustAccountNut.getHome().create(custId, custId, custId,
							CustAccountBean.ACCTYPE_CORPORATE, TimeFormat.getTimestamp(), usrid);
				}
				// Check if SalesRep exist, else create new
				Integer thisSalesRepId = UserNut.getUserId(salesRepId);
				if (thisSalesRepId == null)
				{
					Log.printDebug(count + ": Sales Rep = " + salesRepId + " doesn't exist!!. Creating new ... ");
					thisSalesRepId = createNewSRep(salesRepId, salesRepId);
					// continue;
				}
				// Add docNo into the remarks
				String remarks = comment + "(Old DocRef = " + docNo + ")";
				String thisCurr = (String) hmCurr.get(new Integer(currId));
				if (!thisCurr.equals("MYR"))
				{
					remarks += ", (CURR=" + thisCurr + ")";
				}
				// Derive the LAmt instead of trusting the lTxAmt
				/*
				 * BigDecimal derivedLAmt =
				 * txAmt.divide(exchRate,BigDecimal.ROUND_HALF_UP);
				 */
				BigDecimal derivedLAmt = lDebitAmt;
				if (txType.equals("I"))
				{
					// This is an Invoice
					Log.printDebug(count + ": Processing Invoice ... ");
					if (txStatus.equals("D"))
					{
						Log.printDebug("STATUS = D, Skipping this entry ... ");
						continue;
					}
					/*
					 * DEPRECATED // Create Sales Txn Log.printVerbose(count + ":
					 * Creating SalesTxn ... "); SalesTxn newSalesTxn =
					 * SalesTxnNut.getHome().create(thisCustAcc.getPkid(),
					 * iDefSvcCtrId,txDate,remarks,SalesTxnBean.ST_CREATED,
					 * "",new Integer(0),TimeFormat.getTimestamp(),usrid);
					 * if(txStatus.equals("D"))
					 * newSalesTxn.setStatus(SalesTxnBean.STATUS_CANCELLED);
					 */
					/*
					 * SUPPRESS CREATION OF SALESORDER AND DO // Automatically
					 * create a SalesOrder Log.printVerbose(count + ": Creating
					 * SalesOrder ... "); Jobsheet newSO =
					 * //JobsheetNut.getHome().create(newSalesTxn.getPkid(),
					 * JobsheetNut.getHome().create(new Long(0),
					 * thisCurr,thisSalesRepId,thisSalesRepId,remarks,
					 * JobsheetBean.TYPE_SO,JobsheetBean.STATE_DO_OK,
					 * TimeFormat.getTimestamp(),TimeFormat.getTimestamp(),
					 * usrid); if(txStatus.equals("D"))
					 * newSO.setStatus(JobsheetBean.STATUS_CANCELLED);
					 *  // Query for Invoice Details from CDHisD String
					 * txnDQuery = "select * from cdhisd where docref = '" +
					 * docNo + "'"; Statement txnDStmt = con.createStatement();
					 * ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					 *  // Create DO if row > 0 DeliveryOrder newDO = null; if
					 * (rsTxnD.getFetchSize() > 0) { newDO =
					 * DeliveryOrderNut.getHome().create( //
					 * newSalesTxn.getPkid(), // deprecated new Long(0),
					 * thisSalesRepId,thisSalesRepId,remarks,
					 * DeliveryOrderBean.STATE_CREATED,
					 * txDate,TimeFormat.getTimestamp(),usrid);
					 * if(txStatus.equals("D"))
					 * newDO.setStatus(DeliveryOrderBean.STATUS_CANCELLED);
					 *  // Advance the SO state to DO_OK
					 * newSO.setState(JobsheetBean.STATE_DO_OK); }
					 */
					// Query for Invoice Details from CDHisD
					String txnDQuery = "select * from cdhisd where docref = '" + docNo + "'";
					Statement txnDStmt = con.createStatement();
					ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					// Automatically create an Invoice
					Log.printVerbose(count + ": Creating Invoice ... ");
					// BigDecimal derivedLAmt =
					// txAmt.divide(exchRate,BigDecimal.ROUND_HALF_UP);
					if (derivedLAmt.signum() == 0)
					{
						// Use the lDebitAmt instead
						derivedLAmt = lDebitAmt;
					}
					Invoice newInvoice = null;
					InvoiceObject newInvObj = new InvoiceObject();
					/** Populate the InvoiceObject * */
					// newInvObj.mPkid = new Long("0");
					// newInvObj.mStmtNumber= new Long("0");
					// newInvObj.mSalesTxnId = // deprecated
					// newInvObj.mPaymentTermsId = InvoiceBean.PAYMENT_CASH;
					newInvObj.mTimeIssued = txDate;
					// newInvObj.mCurrency = thisCurr;
					newInvObj.mCurrency = "MYR";
					newInvObj.mTotalAmt = derivedLAmt;
					newInvObj.mOutstandingAmt = derivedLAmt;
					newInvObj.mRemarks = remarks;
					newInvObj.mState = InvoiceBean.ST_POSTED;
					// newInvObj.mStatus = InvoiceBean.STATUS_ACTIVE;
					// newInvObj.mLastUpdate = TimeFormat.getTimestamp();
					newInvObj.mUserIdUpdate = usrid;
					newInvObj.mEntityTable = CustAccountBean.TABLENAME;
					newInvObj.mEntityKey = thisCustAcc.getPkid();
					newInvObj.mEntityName = thisCustAcc.getName(); // ???
					// newInvObj.mEntityType = ""; // ???
					// newInvObj.mIdentityNumber = "";
					newInvObj.mEntityContactPerson = newInvObj.mEntityName;
					// newInvObj.mForeignTable = "";
					// newInvObj.mForeignKey = new Integer(0);
					// newInvObj.mForeignText = "";
					// In order to derive the locationId and pccenter,
					// need to get the custsvcObject
					newInvObj.mCustSvcCtrId = iDefSvcCtrId;
					CustServiceCenterObject thisCSCObj = CustServiceCenterNut.getObject(newInvObj.mCustSvcCtrId);
					newInvObj.mLocationId = thisCSCObj.invLocationId;
					newInvObj.mPCCenter = thisCSCObj.accPCCenterId;
					// newInvObj.mTxnType = "";
					// newInvObj.mStmtType = "";
					// newInvObj.mReferenceNo = "";
					// newInvObj.mDescription = "";
					// newInvObj.mWorkOrder = new Long(0);
					// newInvObj.mDeliveryOrder = new Long(0);
					// newInvObj.mReceiptId = new Long(0);
					// newInvObj.mDisplayFormat = "inv1";
					// newInvObj.mDocType = "inv";
					newInvoice = InvoiceNut.fnCreate(newInvObj);
					// / ADDED BY VINCENT LEE - 2005-06-15
					{
						NominalAccountObject naObj = NominalAccountNut.getObject(CustAccountBean.TABLENAME,
								newInvObj.mPCCenter, newInvObj.mEntityKey, newInvObj.mCurrency);
						if (naObj == null)
						{
							naObj = new NominalAccountObject();
							// code = "not_used";
							naObj.namespace = NominalAccountBean.NS_CUSTOMER;
							naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
							naObj.foreignKey = newInvObj.mEntityKey;
							naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
							naObj.currency = newInvObj.mCurrency;
							naObj.amount = new BigDecimal(0);
							naObj.remarks = newInvObj.mRemarks;
							naObj.accPCCenterId = newInvObj.mPCCenter;
							naObj.userIdUpdate = newInvObj.mUserIdUpdate;
							NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
							naObj = naEJB.getObject();
						}
						NominalAccountTxnObject natObj = new NominalAccountTxnObject();
						natObj.nominalAccount = naObj.pkid;
						natObj.foreignTable = InvoiceBean.TABLENAME;
						natObj.foreignKey = newInvObj.mPkid;
						natObj.code = "not_used";
						natObj.info1 = " ";
						// natObj.description = " ";
						natObj.description = newInvObj.mRemarks;
						natObj.txnType = " ";
						natObj.glCodeDebit = GLCodeBean.ACC_RECEIVABLE;
						natObj.glCodeCredit = GLCodeBean.GENERAL_SALES;
						natObj.currency = newInvObj.mCurrency;
						natObj.amount = newInvObj.mTotalAmt;
						natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
						natObj.timeParam1 = newInvObj.mTimeIssued;
						natObj.timeOption2 = NominalAccountTxnBean.TIME_DUE;
						natObj.timeParam2 = newInvObj.mTimeIssued;
						natObj.state = NominalAccountTxnBean.ST_ACTUAL;
						natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
						natObj.lastUpdate = TimeFormat.getTimestamp();
						natObj.userIdUpdate = newInvObj.mUserIdUpdate;
						NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
					}
					/*
					 * if(txStatus.equals("D"))
					 * newInvoice.setStatus(InvoiceBean.STATUS_CANCELLED);
					 */
					// populate the exchrate
					// newInvoice.setXRate(exchRate);
					Long newInvoiceId = newInvoice.getPkid();
					// Create Items under the Invoice
					int itemCount = 0;
					while (rsTxnD.next())
					{
						itemCount++;
						Log.printVerbose(count + ": Creating SalesOrder Item " + itemCount);
						String itemDocRef = rsTxnD.getString("docRef");
						String itemItemNo = rsTxnD.getString("itemno");
						String itemTxDate = rsTxnD.getString("txdate");
						String itemGlId = rsTxnD.getString("glid");
						String itemStkId = rsTxnD.getString("stkId");
						String itemLocId = rsTxnD.getString("locId");
						BigDecimal itemQty = rsTxnD.getBigDecimal("qty");
						String itemUom = rsTxnD.getString("uom");
						BigDecimal itemUnitPrice = rsTxnD.getBigDecimal("unitprice");
						BigDecimal itemItemTotal = rsTxnD.getBigDecimal("itemtotal");
						BigDecimal itemLItemTotal = rsTxnD.getBigDecimal("litemtotal");
						BigDecimal itemNetSales = rsTxnD.getBigDecimal("netsales");
						BigDecimal itemLNetSales = rsTxnD.getBigDecimal("lnetsales");
						BigDecimal itemLTotalCost = rsTxnD.getBigDecimal("ltotalcost");
						String itemDescOrig = rsTxnD.getString("desc1");
						String itemDesc = itemDescOrig;
						// Add itemGlId into itemDesc
						// itemDesc = itemDesc + ", (GLID=" + itemGlId + ")";
						// Find the InvItem
						if (!itemStkId.trim().equals(""))
						{
							// Use the general non-inv code for the time being
							Item thisItem = ItemNut.getObjectByCode(itemStkId);
							if (thisItem == null)
							{
								Log.printDebug(count + ": Item Code " + itemStkId
										+ " doesn't exist!! Creating a temp one ...");
								// continue;
								/**
								 * *** BEGIN: CREATE THE ITEM BASED ON CDHISD
								 * ****
								 */
								ItemObject itemObj = new ItemObject();
								// populate the properties here!!
								itemObj.code = itemStkId;
								itemObj.name = itemDesc;
								itemObj.description = itemDesc;
								itemObj.userIdUpdate = usrid;
								itemObj.enumInvType = new Integer(ItemBean.INV_TYPE_INVENTORY);
								itemObj.uom = itemUom;
								/*
								 * itemObj.priceList = sellingPrice;
								 * itemObj.priceSale = sellingPrice;
								 * itemObj.priceDisc1 = sellingPrice;
								 * itemObj.priceDisc2 = sellingPrice;
								 * itemObj.priceDisc3 = sellingPrice;
								 * itemObj.priceMin = costPrice;
								 * itemObj.fifoUnitCost = costPrice;
								 * itemObj.maUnitCost = costPrice;
								 * itemObj.waUnitCost = costPrice;
								 * itemObj.lastUnitCost = costPrice;
								 * itemObj.replacementUnitCost = costPrice;
								 * itemObj.preferredSupplier = suppAccObj.pkid;
								 */
								thisItem = ItemNut.fnCreate(itemObj);
								// Then populate the corresponding stock table
								Integer thisLocId = iDefLocId;
								if (!itemLocId.equals(""))
								{
									Location thisLoc = LocationNut.getObjectByCode(itemLocId);
									if (thisLoc != null)
										thisLocId = thisLoc.getPkid();
								}
								Stock auxStk =
								// StockNut.getObjectBy(thisItem.getPkid(),iDefLocId,
								StockNut.getObjectBy(thisItem.getPkid(), thisLocId, iDefCondId, "");
								if (auxStk == null)
								{
									StockObject newObj = new StockObject();
									newObj.itemId = thisItem.getPkid();
									newObj.locationId = thisLocId;
									newObj.accPCCenterId = iDefPCCenterId;
									newObj.userIdUpdate = usrid;
									auxStk = StockNut.fnCreate(newObj);
								}
								/** *** END: CREATE THE ITEM BASED ON CDHISD **** */
							} // end if (thisitem == null)
							// Get the corresponding POSItem
							/*
							 * if (thisPOSItem == null) { Log.printDebug(count + ":
							 * POSItem with itemid " + thisItem.getPkid() + "
							 * doesn't exist!!"); continue; }
							 */
						} // end if (stkId != "")
						else
						{
							// Assume general non-inventory
						}
						/*
						 * SUPPRESS CREATION OF JS and DO Items // Create
						 * JobsheetItem JobsheetItem newJSItem =
						 * JobsheetItemNut.getHome().create(
						 * newSO.getPkid(),thisPOSItemId,
						 * itemQty,thisCurr,itemDesc,itemUnitPrice,itemUnitPrice);
						 * if(txStatus.equals("D"))
						 * newJSItem.setStatus(JobsheetItemBean.STATUS_CANCELLED);
						 *  // Create DOItem DeliveryOrderItem newDOItem =
						 * DeliveryOrderItemNut.getHome().create(
						 * newDO.getPkid(),newJSItem.getPkid(),
						 * itemQty,itemDesc); if(txStatus.equals("D"))
						 * newDOItem.setStatus(DeliveryOrderItemBean.STATUS_CANCELLED);
						 */
						// Create InvoiceItem
						InvoiceItemObject invItemObj = new InvoiceItemObject();
						invItemObj.mInvoiceId = newInvoiceId;
						invItemObj.mTotalQty = itemQty;
						// invItemObj.mCurrency = thisCurr;
						invItemObj.mCurrency = "MYR";
						invItemObj.mRemarks = itemDesc;
						invItemObj.mUnitPriceQuoted = itemUnitPrice;
						InvoiceItem newInvoiceItem = InvoiceItemNut.fnCreate(invItemObj);
						/*
						 * if(txStatus.equals("D"))
						 * newInvoiceItem.setStatus(InvoiceItemBean.STATUS_CANCELLED);
						 */
					} // end while (rsTxnD.next())
					txnDStmt.close();
					// Do some reconciliation here,
					// basically, trust the txAmt in CDHis, and not the
					// InvoiceItems
					BigDecimal totalInvItemAmt = InvoiceNut.getInvoiceAmount(newInvoiceId);
					BigDecimal invAmt = InvoiceNut.getHandle(newInvoiceId).getTotalAmt();
					BigDecimal diff = invAmt.subtract(totalInvItemAmt);
					if (diff.signum() != 0)
					{
						// Add a non-inv item to the invoice balance it up
						// Create InvoiceItem
						String auxRemarks = "Difference between TxAmt and ItemTotal";
						InvoiceItemObject invItemObj = new InvoiceItemObject();
						invItemObj.mInvoiceId = newInvoiceId;
//						invItemObj.mPosItemId = POSItemBean.PKID_NONINV;
						invItemObj.mTotalQty = new BigDecimal(1.00);
						// invItemObj.mCurrency = thisCurr;
						invItemObj.mCurrency = "MYR";
						invItemObj.mRemarks = auxRemarks;
						invItemObj.mUnitPriceQuoted = diff;
						// POSItemChildObject posChild =
						// POSItemNut.getChildObject(thisPOSItem.getPkid());
//						invItemObj.mPosItemType = POSItemBean.TYPE_NINV;
						// invItemObj.mItemId = posChild.itemFKId;
						// invItemObj.mItemCode = posChild.code;
						// invItemObj.mName= posChild.name;
						InvoiceItem auxInvoiceItem = InvoiceItemNut.fnCreate(invItemObj);
						/*
						 * if(txStatus.equals("D"))
						 * auxInvoiceItem.setStatus(InvoiceItemBean.STATUS_CANCELLED);
						 */
					}
					/*
					 * // Post the Invoice to Nominal Account immediately //
					 * Alex: 06/23 - Post ONLY when tx is NOT DELETED // *******
					 * BEGIN POSTING *********** if (!txStatus.equals("D")) {
					 * NominalAccountObject naObj = NominalAccountNut.getObject(
					 * NominalAccountBean.FT_CUSTOMER, //iDefPCCenterId,
					 * thisCustAcc.getPkid(), thisCurr); iDefPCCenterId,
					 * thisCustAcc.getPkid(), "MYR"); // only one nominal
					 * account (MYR) even though transactions are in foreign
					 * currencies (info captured in invoice already) // Get the
					 * Invoice Amount // BigDecimal bdInvoiceAmt =
					 * InvoiceNut.getInvoiceAmount(newInvoiceId);
					 * 
					 * if(naObj==null) { naObj = new NominalAccountObject();
					 * //naObj.pkid = new Integer("0"); //naObj.code = new
					 * String("not_used"); naObj.namespace =
					 * NominalAccountBean.NS_CUSTOMER; naObj.foreignTable =
					 * NominalAccountBean.FT_CUSTOMER; naObj.foreignKey =
					 * thisCustAcc.getPkid(); naObj.accountType =
					 * NominalAccountBean.ACC_TYPE_RECEIVABLE; //naObj.currency =
					 * thisCurr; naObj.currency = "MYR"; // always use MYR for
					 * Topcon's case naObj.amount = new BigDecimal("0.00");
					 * //naObj.amount = naObj.amount.add(invAmt);
					 * //naObj.remarks = " "; naObj.remarks = remarks;
					 * naObj.accPCCenterId = iDefPCCenterId; naObj.state =
					 * NominalAccountBean.STATE_CREATED; naObj.status =
					 * NominalAccountBean.STATUS_ACTIVE; naObj.lastUpdate =
					 * TimeFormat.getTimestamp(); naObj.userIdUpdate = usrid;
					 * NominalAccount naEJB = NominalAccountNut.fnCreate(naObj); } //
					 * Gather necessary info to create the nominal txn
					 *  // Create the nominal account txn
					 * NominalAccountTxnObject natObj = new
					 * NominalAccountTxnObject(); natObj.nominalAccount =
					 * naObj.pkid; // Primary Key natObj.foreignTable =
					 * InvoiceBean.TABLENAME; natObj.foreignKey = newInvoiceId;
					 * natObj.code = "not_used"; natObj.info1 = " ";
					 * natObj.description = remarks; natObj.txnType =" ";
					 * natObj.glCodeDebit=NominalAccountBean.GLCODE_NOMINAL;
					 * natObj.glCodeCredit= GLCodeBean.GENERAL_SALES;
					 * natObj.currency = thisCurr; //natObj.currency = "MYR"; //
					 * always use MYR for Topcon's case natObj.amount = invAmt;
					 * natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
					 * natObj.timeParam1 = txDate; natObj.timeOption2 =
					 * NominalAccountTxnBean.TIME_DUE; natObj.timeParam2 =
					 * txDate; natObj.state = NominalAccountTxnBean.ST_ACTUAL;
					 * natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
					 * natObj.lastUpdate = TimeFormat.getTimestamp();
					 * natObj.userIdUpdate = usrid;
					 * 
					 * NominalAccountTxn natEJB =
					 * NominalAccountTxnNut.fnCreate(natObj); } // end if
					 * (txStatus != Deleted)
					 *  // ******* END POSTING ***********
					 */
				} // end if(txType = I) // INVOICE
				else if (txType.equals("P") && !txStatus.equals("D")) // for
																		// Payment
				{
					Log.printDebug("*** Detected a Receipt ***");
					// Query for Details from CDHisD
					String txnDQuery = "select * from cdhisd where docref = '" + docNo + "'";
					Statement txnDStmt = con.createStatement();
					ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					String strPayMethod = "cash"; // default to cash
					String strChequeNo = "";
					Integer iCashAccId = iDefCashAccId;
					// while(rsTxnD.next())
					if (rsTxnD.next())
					{
						// String glId = rsTxnD.getString("glid");
						String desc1 = rsTxnD.getString("desc1").trim();
						if (!desc1.equals("") && !desc1.startsWith("0/"))
						{
							strPayMethod = "cheque";
							strChequeNo = desc1;
							iCashAccId = iDefChequeAccId;
						}
						// remarks = remarks + ", (GLID=" + glId
						remarks = remarks + " (DESC=" + desc1 + ")";
					}
					txnDStmt.close();
					NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER,
					// iDefPCCenterId, thisCustAcc.getPkid(), thisCurr);
							iDefPCCenterId, thisCustAcc.getPkid(), "MYR");
					if (naObj == null)
					{
						naObj = new NominalAccountObject();
						// naObj.pkid = new Integer("0");
						// naObj.code = new String("not_used");
						naObj.namespace = NominalAccountBean.NS_CUSTOMER;
						naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
						naObj.foreignKey = thisCustAcc.getPkid();
						naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
						// naObj.currency = thisCurr;
						naObj.currency = "MYR";
						// naObj.amount = naObj.amount.add(lTxAmt);
						naObj.remarks = remarks;
						naObj.accPCCenterId = iDefPCCenterId;
						naObj.state = NominalAccountBean.STATE_CREATED;
						naObj.status = NominalAccountBean.STATUS_ACTIVE;
						naObj.lastUpdate = TimeFormat.getTimestamp();
						naObj.userIdUpdate = usrid;
						NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
					}
					/*
					 * // if derivedLAmt = 0, take the sum from payrefd if
					 * (derivedLAmt.signum() == 0) { derivedLAmt =
					 * getPaymentSum(con, docNo); Log.printVerbose(docNo + ":
					 * derivedLAmt = " + derivedLAmt); }
					 */
					// Alex (07/03) - must get from payrefd ALL THE TIME,
					// since we don't know if the payment if for multiple
					// customers
					// Vector vecPayment = getPaymentSum(con, docNo, thisCurr,
					// remarks, usrid);
					Vector vecPayment = getPaymentSum(con, docNo, "MYR", remarks, usrid);
					if (vecPayment.size() == 0)
					{
						Long newRcptId = createCustReceipt(naObj.pkid, iCashAccId, txDate, txDate, txDate,
						// thisCurr,lCreditAmt,strPayMethod,remarks,
								"MYR", lCreditAmt, strPayMethod, remarks, strChequeNo, // ""
																						// for
																						// Cash
								usrid, TimeFormat.getTimestamp());
					} else
					{
						for (int pymtCnt = 0; pymtCnt < vecPayment.size(); pymtCnt++)
						{
							Log.printVerbose("*** pymtCnt = " + pymtCnt + " ***");
							Payment thisPymt = (Payment) vecPayment.get(pymtCnt);
							// Create a Receipt
							Long newRcptId = createCustReceipt(thisPymt.nomAccId, iCashAccId, txDate, txDate, txDate,
							// thisCurr,thisPymt.amt,strPayMethod,remarks,
									"MYR", thisPymt.amt, strPayMethod, remarks, strChequeNo, // ""
																								// for
																								// Cash
									usrid, TimeFormat.getTimestamp());
						} // end for
					} // end if (vecPayment.size() == 0)
					/*
					 * // Find the invoices mapped to this receipt String
					 * doc2DocQ = "select * from payrefd where payno = '" +
					 * docNo + "'"; Statement doc2DocStmt =
					 * con.createStatement(); ResultSet rsDoc2Doc =
					 * doc2DocStmt.executeQuery(doc2DocQ);
					 * 
					 * String findInvQ = "select pkid from cust_invoice_index
					 * where remarks ~ ?"; PreparedStatement findInvStmt =
					 * jbossCon.prepareStatement(findInvQ);
					 * 
					 * while (rsDoc2Doc.next()) { String payNo =
					 * rsDoc2Doc.getString("payno"); String payType =
					 * rsDoc2Doc.getString("paytype"); String payInfo =
					 * rsDoc2Doc.getString("payinfo"); BigDecimal
					 * payRefDExchRate = rsDoc2Doc.getBigDecimal("exch_rate");
					 * String payRefDTxRefNo = rsDoc2Doc.getString("txrefno");
					 * String payRefDTxInfo = rsDoc2Doc.getString("txinfo");
					 * String payRefTxType = rsDoc2Doc.getString("txType");
					 * BigDecimal payRefDTxExchRate =
					 * rsDoc2Doc.getBigDecimal("txexch_rate"); Timestamp
					 * payRefDTxDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("txdate"), "MM/dd/yy HH:mm:ss");
					 * Timestamp payDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("paydate"), "MM/dd/yy HH:mm:ss");
					 * BigDecimal payAmt = rsDoc2Doc.getBigDecimal("payamt");
					 * BigDecimal lPayAmt = rsDoc2Doc.getBigDecimal("lpayamt");
					 * BigDecimal payRefDLTxAmt =
					 * rsDoc2Doc.getBigDecimal("ltxamt"); String posted =
					 * rsDoc2Doc.getString("posted"); String mthEnd =
					 * rsDoc2Doc.getString("mthend"); String payRefDCustId =
					 * rsDoc2Doc.getString("custId");
					 *  // Find the Invoice PKID corresponding to txRefNo
					 * findInvStmt.setString(1, payRefDTxRefNo); ResultSet
					 * rsFindInv = findInvStmt.executeQuery(); Long thisInvId =
					 * null; if(rsFindInv.next()) { thisInvId = new
					 * Long(rsFindInv.getLong("pkid"));
					 *  // Create the DocLink DocLink newDocLink =
					 * DocLinkNut.getHome().create( DocLinkBean.NS_CUSTOMER, "",
					 * DocLinkBean.RELTYPE_PAYMENT, ReceiptBean.TABLENAME,
					 * newRcptId, InvoiceBean.TABLENAME, thisInvId, thisCurr,
					 * lPayAmt, "", payAmt, "", TimeFormat.getTimestamp(),
					 * usrid); } else { //throw new Exception("Could not find
					 * Invoice " + payRefDTxRefNo); Log.printDebug("Could not
					 * find Invoice " + payRefDTxRefNo); } findInvStmt.close(); }
					 * doc2DocStmt.close();
					 */
				} // end if(txType = P) // PAYMENT
				else if (txType.equals("C") && !txStatus.equals("D")) // for
																		// CreditNote
				{
					Log.printDebug("*** Detected a CreditNote ***");
					// Query for Details from CDHisD
					String txnDQuery = "select * from cdhisd where docref = '" + docNo + "'";
					Statement txnDStmt = con.createStatement();
					ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					String creditNoteRemarks = remarks;
					while (rsTxnD.next())
					{
						String glId = rsTxnD.getString("glid");
						String stkId = rsTxnD.getString("stkid");
						String desc1 = rsTxnD.getString("desc1");
						if (!stkId.equals(""))
						{
							creditNoteRemarks = creditNoteRemarks + "," + stkId;
						} else
						{
							// remarks = remarks + ", (GLID=" + glId
							remarks = remarks + " (DESC=" + desc1 + ")";
							// creditNoteRemarks = creditNoteRemarks + ",
							// (GLID=" + glId
							creditNoteRemarks = creditNoteRemarks + " (DESC=" + desc1 + ")";
						}
						// Truncate remarks & creditNoteRemarks to 500 char
						if (remarks.length() > NominalAccountBean.MAX_LEN_REMARKS)
						{
							remarks = remarks.substring(0, NominalAccountBean.MAX_LEN_REMARKS);
						}
						if (creditNoteRemarks.length() > GenericStmtBean.MAX_LEN_REMARKS)
						{
							creditNoteRemarks = creditNoteRemarks.substring(0, GenericStmtBean.MAX_LEN_REMARKS);
						}
					}
					txnDStmt.close();
					NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER,
					// iDefPCCenterId, thisCustAcc.getPkid(), thisCurr);
							iDefPCCenterId, thisCustAcc.getPkid(), "MYR");
					if (naObj == null)
					{
						naObj = new NominalAccountObject();
						// naObj.pkid = new Integer("0");
						// naObj.code = new String("not_used");
						naObj.namespace = NominalAccountBean.NS_CUSTOMER;
						naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
						naObj.foreignKey = thisCustAcc.getPkid();
						naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
						// naObj.currency = thisCurr;
						naObj.currency = "MYR";
						// naObj.amount = naObj.amount.add(lTxAmt);
						naObj.remarks = remarks;
						naObj.accPCCenterId = iDefPCCenterId;
						naObj.state = NominalAccountBean.STATE_CREATED;
						naObj.status = NominalAccountBean.STATUS_ACTIVE;
						naObj.lastUpdate = TimeFormat.getTimestamp();
						naObj.userIdUpdate = usrid;
						NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
					}
					/*
					 * // if derivedLAmt = 0, take the sum from payrefd if
					 * (derivedLAmt.signum() == 0) { derivedLAmt =
					 * getPaymentSum(con, docNo); Log.printVerbose(docNo + ":
					 * derivedLAmt = " + derivedLAmt); }
					 *  // Create a Credit Note Long newCNId = createCreditNote(
					 * naObj.pkid, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
					 * GLCodeBean.CASH_DISCOUNT, txDate, InvoiceBean.TABLENAME,
					 * new Long(0), docNo, creditNoteRemarks,
					 * TimeFormat.getTimestamp(), usrid, derivedLAmt);
					 */
					// Alex (07/03) - must get from payrefd ALL THE TIME,
					// since we don't know if the payment if for multiple
					// customers
					// Vector vecPayment = getPaymentSum(con, docNo, thisCurr,
					// remarks,usrid);
					Vector vecPayment = getPaymentSum(con, docNo, "MYR", remarks, usrid);
					if (vecPayment.size() == 0)
					{
						Long newCNId = createCreditNote(naObj.pkid, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
								GLCodeBean.CASH_DISCOUNT, txDate, InvoiceBean.TABLENAME, new Long(0), docNo,
								creditNoteRemarks, TimeFormat.getTimestamp(),
								// usrid, thisCurr, lCreditAmt);
								usrid, "MYR", lCreditAmt);
					} else
					{
						for (int pymtCnt = 0; pymtCnt < vecPayment.size(); pymtCnt++)
						{
							Log.printVerbose("*** pymtCnt = " + pymtCnt + " ***");
							Payment thisPymt = (Payment) vecPayment.get(pymtCnt);
							// Create a Credit Note
							Long newCNId = createCreditNote(
							// naObj.pkid,
							// GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
									thisPymt.nomAccId, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
									GLCodeBean.CASH_DISCOUNT, txDate, InvoiceBean.TABLENAME, new Long(0), docNo,
									creditNoteRemarks, TimeFormat.getTimestamp(),
									// usrid, thisCurr, thisPymt.amt);
									usrid, "MYR", thisPymt.amt);
						} // end for
					} // end if (vecPayment.size() == 0)
					/*
					 * // Find the invoices mapped to this receipt String
					 * doc2DocQ = "select * from payrefd where payno = '" +
					 * docNo + "'"; Statement doc2DocStmt =
					 * con.createStatement(); ResultSet rsDoc2Doc =
					 * doc2DocStmt.executeQuery(doc2DocQ);
					 * 
					 * String findInvQ = "select pkid from cust_invoice_index
					 * where remarks ~ ?"; PreparedStatement findInvStmt =
					 * jbossCon.prepareStatement(findInvQ);
					 * 
					 * while (rsDoc2Doc.next()) { String payNo =
					 * rsDoc2Doc.getString("payno"); String payType =
					 * rsDoc2Doc.getString("paytype"); String payInfo =
					 * rsDoc2Doc.getString("payinfo"); BigDecimal
					 * payRefDExchRate = rsDoc2Doc.getBigDecimal("exch_rate");
					 * String payRefDTxRefNo = rsDoc2Doc.getString("txrefno");
					 * String payRefDTxInfo = rsDoc2Doc.getString("txinfo");
					 * String payRefTxType = rsDoc2Doc.getString("txType");
					 * BigDecimal payRefDTxExchRate =
					 * rsDoc2Doc.getBigDecimal("txexch_rate"); Timestamp
					 * payRefDTxDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("txdate"), "MM/dd/yy HH:mm:ss");
					 * Timestamp payDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("paydate"), "MM/dd/yy HH:mm:ss");
					 * BigDecimal payAmt = rsDoc2Doc.getBigDecimal("payamt");
					 * BigDecimal lPayAmt = rsDoc2Doc.getBigDecimal("lpayamt");
					 * BigDecimal payRefDLTxAmt =
					 * rsDoc2Doc.getBigDecimal("ltxamt"); String posted =
					 * rsDoc2Doc.getString("posted"); String mthEnd =
					 * rsDoc2Doc.getString("mthend"); String payRefDCustId =
					 * rsDoc2Doc.getString("custId");
					 *  // Find the Invoice PKID corresponding to txRefNo
					 * findInvStmt.setString(1, payRefDTxRefNo); ResultSet
					 * rsFindInv = findInvStmt.executeQuery(); Long thisInvId =
					 * null; if(rsFindInv.next()) { thisInvId = new
					 * Long(rsFindInv.getLong("pkid"));
					 *  // Create the DocLink DocLink newDocLink =
					 * DocLinkNut.getHome().create( DocLinkBean.NS_CUSTOMER, "",
					 * DocLinkBean.RELTYPE_CN, GenericStmtBean.TABLENAME,
					 * newCNId, InvoiceBean.TABLENAME, thisInvId, thisCurr,
					 * lPayAmt, "", payAmt, "", TimeFormat.getTimestamp(),
					 * usrid); } else { //throw new Exception("Could not find
					 * Invoice " + payRefDTxRefNo); Log.printDebug("Could not
					 * find Invoice " + payRefDTxRefNo); } findInvStmt.close(); }
					 * doc2DocStmt.close();
					 */
				} // end if(txType = C) // CreditNote
				else if (txType.equals("D") && !txStatus.equals("D")) // for
																		// DebitNote
				{
					Log.printDebug("*** Detected a DebitNote ***");
					// Query for Details from CDHisD
					String txnDQuery = "select * from cdhisd where docref = '" + docNo + "'";
					Statement txnDStmt = con.createStatement();
					ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					String debitNoteRemarks = remarks;
					while (rsTxnD.next())
					{
						String glId = rsTxnD.getString("glid");
						String stkId = rsTxnD.getString("stkid");
						String desc1 = rsTxnD.getString("desc1");
						if (!stkId.equals(""))
						{
							debitNoteRemarks = debitNoteRemarks + "," + stkId;
						} else
						{
							// remarks = remarks + ", (GLID=" + glId
							remarks = remarks + " (DESC=" + desc1 + ")";
							// debitNoteRemarks = debitNoteRemarks + ", (GLID="
							// + glId
							debitNoteRemarks = debitNoteRemarks + " (DESC=" + desc1 + ")";
						}
						// Truncate debitNoteRemarks to 500 char
						if (debitNoteRemarks.length() > GenericStmtBean.MAX_LEN_REMARKS)
						{
							debitNoteRemarks = debitNoteRemarks.substring(0, GenericStmtBean.MAX_LEN_REMARKS);
						}
					}
					txnDStmt.close();
					NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER,
					// iDefPCCenterId, thisCustAcc.getPkid(), thisCurr);
							iDefPCCenterId, thisCustAcc.getPkid(), "MYR");
					if (naObj == null)
					{
						naObj = new NominalAccountObject();
						// naObj.pkid = new Integer("0");
						// naObj.code = new String("not_used");
						naObj.namespace = NominalAccountBean.NS_CUSTOMER;
						naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
						naObj.foreignKey = thisCustAcc.getPkid();
						naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
						// naObj.currency = thisCurr;
						naObj.currency = "MYR";
						// naObj.amount = naObj.amount.add(lTxAmt);
						naObj.remarks = remarks;
						naObj.accPCCenterId = iDefPCCenterId;
						naObj.state = NominalAccountBean.STATE_CREATED;
						naObj.status = NominalAccountBean.STATUS_ACTIVE;
						naObj.lastUpdate = TimeFormat.getTimestamp();
						naObj.userIdUpdate = usrid;
						NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
					}
					// if derivedLAmt = 0, take the sum from payrefd
					if (derivedLAmt.signum() == 0)
					{
						// derivedLAmt = getRevPaymentSum(con, docNo);
						derivedLAmt = lDebitAmt;
						Log.printVerbose(docNo + ": derivedLAmt = " + derivedLAmt);
					}
					// Create a Debit Note
					Long newDNId = createDebitNote(naObj.pkid, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
							GLCodeBean.INTEREST_REVENUE, txDate, InvoiceBean.TABLENAME, new Long(0), docNo,
							debitNoteRemarks, TimeFormat.getTimestamp(),
							// usrid, thisCurr, derivedLAmt);
							usrid, "MYR", derivedLAmt);
					/*
					 * // Find the invoices mapped to this receipt String
					 * doc2DocQ = "select * from payrefd where payno = '" +
					 * docNo + "'"; Statement doc2DocStmt =
					 * con.createStatement(); ResultSet rsDoc2Doc =
					 * doc2DocStmt.executeQuery(doc2DocQ);
					 * 
					 * String findInvQ = "select pkid from cust_invoice_index
					 * where remarks ~ ?"; PreparedStatement findInvStmt =
					 * jbossCon.prepareStatement(findInvQ);
					 * 
					 * while (rsDoc2Doc.next()) { String payNo =
					 * rsDoc2Doc.getString("payno"); String payType =
					 * rsDoc2Doc.getString("paytype"); String payInfo =
					 * rsDoc2Doc.getString("payinfo"); BigDecimal
					 * payRefDExchRate = rsDoc2Doc.getBigDecimal("exch_rate");
					 * String payRefDTxRefNo = rsDoc2Doc.getString("txrefno");
					 * String payRefDTxInfo = rsDoc2Doc.getString("txinfo");
					 * String payRefTxType = rsDoc2Doc.getString("txType");
					 * BigDecimal payRefDTxExchRate =
					 * rsDoc2Doc.getBigDecimal("txexch_rate"); Timestamp
					 * payRefDTxDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("txdate"), "MM/dd/yy HH:mm:ss");
					 * Timestamp payDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("paydate"), "MM/dd/yy HH:mm:ss");
					 * BigDecimal payAmt = rsDoc2Doc.getBigDecimal("payamt");
					 * BigDecimal lPayAmt = rsDoc2Doc.getBigDecimal("lpayamt");
					 * BigDecimal payRefDLTxAmt =
					 * rsDoc2Doc.getBigDecimal("ltxamt"); String posted =
					 * rsDoc2Doc.getString("posted"); String mthEnd =
					 * rsDoc2Doc.getString("mthend"); String payRefDCustId =
					 * rsDoc2Doc.getString("custId");
					 *  // Find the Invoice PKID corresponding to txRefNo
					 * findInvStmt.setString(1, payRefDTxRefNo); ResultSet
					 * rsFindInv = findInvStmt.executeQuery(); Long thisInvId =
					 * null; if(rsFindInv.next()) { thisInvId = new
					 * Long(rsFindInv.getLong("pkid"));
					 *  // Create the DocLink DocLink newDocLink =
					 * DocLinkNut.getHome().create( DocLinkBean.NS_CUSTOMER, "",
					 * DocLinkBean.RELTYPE_DN, GenericStmtBean.TABLENAME,
					 * newDNId, InvoiceBean.TABLENAME, thisInvId, thisCurr,
					 * lPayAmt, "", payAmt, "", TimeFormat.getTimestamp(),
					 * usrid); } else { //throw new Exception("Could not find
					 * Invoice " + payRefDTxRefNo); Log.printDebug("Could not
					 * find Invoice " + payRefDTxRefNo); } findInvStmt.close(); }
					 * doc2DocStmt.close();
					 */
				} // end if(txType = D) // DebitNote
				else if (txType.equals("R") && !txStatus.equals("D")) // for
																		// ReversePayment
				{
					Log.printDebug("*** Detected a ReversePayment ***");
					// Query for Details from CDHisD
					String txnDQuery = "select * from cdhisd where docref = '" + docNo + "'";
					Statement txnDStmt = con.createStatement();
					ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					while (rsTxnD.next())
					{
						String glId = rsTxnD.getString("glid");
						String desc1 = rsTxnD.getString("desc1");
						// remarks = remarks + ", (GLID=" + glId
						remarks = remarks + " (DESC=" + desc1 + ")";
					}
					txnDStmt.close();
					NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER,
					// iDefPCCenterId, thisCustAcc.getPkid(), thisCurr);
							iDefPCCenterId, thisCustAcc.getPkid(), "MYR");
					if (naObj == null)
					{
						naObj = new NominalAccountObject();
						// naObj.pkid = new Integer("0");
						// naObj.code = new String("not_used");
						naObj.namespace = NominalAccountBean.NS_CUSTOMER;
						naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
						naObj.foreignKey = thisCustAcc.getPkid();
						naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
						// naObj.currency = thisCurr;
						naObj.currency = "MYR";
						// naObj.amount = naObj.amount.add(lTxAmt);
						naObj.remarks = remarks;
						naObj.accPCCenterId = iDefPCCenterId;
						naObj.state = NominalAccountBean.STATE_CREATED;
						naObj.status = NominalAccountBean.STATUS_ACTIVE;
						naObj.lastUpdate = TimeFormat.getTimestamp();
						naObj.userIdUpdate = usrid;
						NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
					}
					/*
					 * // if derivedLAmt = 0, take the sum from payrefd if
					 * (derivedLAmt.signum() == 0) { //derivedLAmt =
					 * getRevPaymentSum(con, docNo); derivedLAmt = lDebitAmt;
					 * Log.printVerbose(docNo + ": derivedLAmt = " +
					 * derivedLAmt); }
					 */
					Vector vecRevPayment = getRevPaymentSum(con, docNo, "MYR", remarks, usrid);
					if (vecRevPayment.size() == 0)
					{
						// Create a ReversePayment
						Long newRPId = createReversePayment(GenericStmtBean.STMT_TYPE_REVERSE_PAYMENT, naObj.pkid,
								iDefCashAccId, "MYR", // thisCurr,
								derivedLAmt, "", remarks, "", txDate, "", new Long(0), usrid);
					} else
					{
						for (int pymtCnt = 0; pymtCnt < vecRevPayment.size(); pymtCnt++)
						{
							Log.printVerbose("*** pymtCnt = " + pymtCnt + " ***");
							Payment thisPymt = (Payment) vecRevPayment.get(pymtCnt);
							// Create a ReversePayment
							Long newRPId = createReversePayment(GenericStmtBean.STMT_TYPE_REVERSE_PAYMENT,
									thisPymt.nomAccId, // naObj.pkid,
									iDefCashAccId, "MYR", // thisCurr,
									thisPymt.amt, // derivedLAmt,
									"", remarks, "", txDate, "", new Long(0), usrid);
						} // end for
					} // end if (vecPayment.size() == 0)
					// Derive the receipt mapped to this reverse payment
					// Note: Only deriving for docno that is in the form
					// R/OR#####
					Log.printVerbose("*** reversePaymentRef = " + docNo + "***");
					/*
					 * if (docNo.startsWith("R/OR")) { String receiptRef =
					 * docNo.substring(2); Log.printVerbose("*** receiptRef = " +
					 * receiptRef + "***");
					 * 
					 * String findRcptQ = "select pkid from cust_receipt_index
					 * where payment_remarks ~ " + "'" + receiptRef + "'";
					 * Statement findRcptStmt = jbossCon.createStatement();
					 * ResultSet rsFindRcpt =
					 * findRcptStmt.executeQuery(findRcptQ);
					 * 
					 * if(rsFindRcpt.next()) { Long thisRcptId = new
					 * Long(rsFindRcpt.getLong("pkid"));
					 *  // Create the DocLink DocLink newDocLink =
					 * DocLinkNut.getHome().create( DocLinkBean.NS_CUSTOMER, "",
					 * DocLinkBean.RELTYPE_REV_PAYMENT,
					 * GenericStmtBean.TABLENAME, newRPId,
					 * ReceiptBean.TABLENAME, thisRcptId, thisCurr, lTxAmt, "",
					 * txAmt, "", TimeFormat.getTimestamp(), usrid); }
					 * findRcptStmt.close(); }
					 */
				} // end if(txType = R) // ReversePayment
				else
				{
					Log.printDebug("*** UNKNOWN txtype ***");
				}
			} // end while(rsCustTxn.next())
			custTxnStmt.close();
			/*******************************************************************
			 * FIXES AFTER POPULATION OF DOC INDICES
			 ******************************************************************/
			// 1. Fix all transactions that had gainloseamt != 0 owing to
			// differing exch_rates
			// fixExRate(con, jbossCon, usrid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while converting Topcon DB: " + ex.getMessage());
		}
		Log.printVerbose("***** END: MIGRATION OF TOPCON DB *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "CREATE DOC INDEX");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		// Clean up curTask;
		curTask = null;
		return new ActionRouter("test-migrate-topcondb-page");
	}

	private Integer createNewSRep(String srId, String srDesc) throws Exception
	{
		// Create the user
		Integer newUsrId = UserNut.getUserId(srId);
		User newUsr = null;
		if (newUsrId != null)
		{
			return newUsrId;
			// newUsr = UserNut.getHandle(newUsrId);
		} else
		{
			newUsr = UserNut.getHome().create(srId, srId, srDesc, "");
		}
		// Create the user details
		Collection colUsrDet = UserDetailsNut.getCollectionByUserId(newUsr.getUserId());
		if (colUsrDet == null || colUsrDet.isEmpty())
		{
			UserDetails thisUserD = UserDetailsNut.getHome().create(newUsr.getUserId(), Calendar.getInstance(), "", "",
					"", "", Calendar.getInstance());
		}
		// Assign a default role for the user
		Collection colUsrRole = ActionDo.getUserRoleHome().findUserRolesGiven("userid", newUsr.getUserId().toString());
		if (colUsrRole == null || colUsrRole.isEmpty())
		{
			ActionDo.getUserRoleHome().create(RoleBean.ROLEID_DEVELOPER, newUsr.getUserId(), Calendar.getInstance());
		}
		return newUsr.getUserId();
	}

	private Integer getUOM(String uom) throws Exception
	{
		Integer thisUOM = new Integer(ItemBean.UOM_NONE);
		if (uom.equals("PCE") || uom.equals("PCS"))
			thisUOM = new Integer(ItemBean.UOM_PCS);
		else if (uom.equals("ROLL"))
			thisUOM = new Integer(ItemBean.UOM_ROLL);
		else if (uom.equals("SET") || uom.equals("SETS") || uom.equals("SSET"))
			thisUOM = new Integer(ItemBean.UOM_SET);
		else if (uom.equals("UNIT") || uom.equals("NOS"))
			thisUOM = new Integer(ItemBean.UOM_UNIT);
		return thisUOM;
	}

	private Long createCustReceipt(Integer naPkid, Integer caPkid, Timestamp tsReceiptDate, Timestamp tsEffFromDate,
			Timestamp tsEffToDate, String currency, BigDecimal bdPaymentAmt, String strPaymentMethod,
			String strPaymentRmks,
			// String strGLCodeCredit,
			// String strGLCodeDebit,
			String strChequeNumber,
			// String strCashAccount,
			Integer iUsrId, Timestamp tsCreate) throws Exception
	{
		Long salesTxnId = new Long("0");
		String strAmountStr = CurrencyFormat.toWords(bdPaymentAmt, "", "");
		strAmountStr = strAmountStr + " ONLY";
		// / Get objects based on parameters above
		NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
		CashAccountObject caObj = CashAccountNut.getObject(caPkid);
		if (defBranchObj == null)
			defBranchObj = BranchNut.getObject(iDefSvcCtrId);
		/*
		 * /// 1) create a printable Cust Receipt Object ReceiptObject cRcptObj =
		 * new ReceiptObject(); cRcptObj.salesTxnId = salesTxnId;
		 * cRcptObj.custAccount = naObj.foreignKey; cRcptObj.intReserved1 = new
		 * Integer("0"); cRcptObj.strReserved1 = ""; //cRcptObj.currency =
		 * naObj.currency; cRcptObj.currency = currency; cRcptObj.amountStr =
		 * strAmountStr; cRcptObj.paymentAmount = bdPaymentAmt;
		 * cRcptObj.paymentMethod = strPaymentMethod; cRcptObj.paymentTime =
		 * tsReceiptDate; cRcptObj.chequeNumber = strChequeNumber;
		 * cRcptObj.bankId = caPkid; cRcptObj.paymentRemarks = strPaymentRmks;
		 * cRcptObj.glCode = GLCodeBean.GENERAL_SALES; cRcptObj.state =
		 * ReceiptBean.ST_CREATED; cRcptObj.status = ReceiptBean.STATUS_ACTIVE;
		 * cRcptObj.lastUpdate = tsCreate; cRcptObj.userIdUpdate = iUsrId;
		 * 
		 * Receipt sPayEJB = ReceiptNut.fnCreate(cRcptObj);
		 */
		// // create the receipt
		OfficialReceiptObject receiptObj = new OfficialReceiptObject();
		receiptObj.entityTable = CustAccountBean.TABLENAME;
		receiptObj.entityKey = naObj.foreignKey;
		receiptObj.entityName = CustAccountNut.getHandle(receiptObj.entityKey).getName();
		receiptObj.currency = currency;
		receiptObj.amount = bdPaymentAmt;
		receiptObj.paymentTime = tsReceiptDate;
		receiptObj.paymentMethod = strPaymentMethod;
		receiptObj.paymentRemarks = strPaymentRmks;
		receiptObj.chequeNumber = strChequeNumber;
		receiptObj.lastUpdate = TimeFormat.getTimestamp();
		receiptObj.userIdUpdate = iUsrId;
		receiptObj.cbCash = defBranchObj.cashbookCash;
		receiptObj.cbCard = defBranchObj.cashbookCard;
		receiptObj.cbCheque = defBranchObj.cashbookCheque;
		receiptObj.cbPDCheque = defBranchObj.cashbookPDCheque;
		if (strPaymentMethod.equals("cheque"))
		{
			receiptObj.amountCheque = bdPaymentAmt;
		} else
		{
			receiptObj.amountCash = bdPaymentAmt;
		}
		// receiptObj.amountCard = this.amountCard;
		// receiptObj.amountPDCheque = this.amountPDCheque;
		// if(this.amountPDCheque.signum()>0)
		// { receiptObj.chequeNumberPD = this.chequeNumberPD;}
		// receiptObj.datePDCheque = this.pdChequeDate;
		receiptObj.branch = defBranchObj.pkid;
		receiptObj.pcCenter = iDefPCCenterId;
		OfficialReceipt offRctEJB = OfficialReceiptNut.fnCreate(receiptObj);
		Log.printVerbose(receiptObj.toString());
		if (offRctEJB == null)
		{
			throw new Exception("Failed to create Receipt!!");
		}
		// / 2) create an entry in the nominal account transaction
		/*
		 * NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		 * natObj.nominalAccount = naObj.pkid; natObj.foreignTable =
		 * NominalAccountTxnBean.FT_CUST_RECEIPT; natObj.foreignKey =
		 * receiptObj.pkid; natObj.code = "not_used"; natObj.info1 = "";
		 * natObj.description = strPaymentRmks; natObj.glCodeCredit =
		 * GLCodeBean.ACC_RECEIVABLE; natObj.glCodeDebit = caObj.accountType;
		 * natObj.currency = naObj.currency; natObj.amount =
		 * bdPaymentAmt.negate(); natObj.timeOption1 =
		 * NominalAccountTxnBean.TIME_STMT; natObj.timeParam1 = tsReceiptDate;
		 * natObj.timeOption2 = NominalAccountTxnBean.TIME_NA; natObj.timeParam2 =
		 * tsReceiptDate; natObj.state = NominalAccountTxnBean.ST_CREATED;
		 * natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
		 * natObj.lastUpdate = tsCreate; natObj.userIdUpdate = iUsrId;
		 * NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
		 * 
		 * if(natEJB == null) { try { offRctEJB.remove();} catch(Exception ex) {
		 * ex.printStackTrace();} throw new Exception("Failed to create NAT"); }
		 * /// 4) update cash account transactions CashAccTxnObject catObj = new
		 * CashAccTxnObject(); catObj.txnCode = CashAccTxnBean.TXN_CODE_DEFAULT;
		 * catObj.glCodeCredit = GLCodeBean.ACC_RECEIVABLE; catObj.glCodeDebit =
		 * caObj.accountType; catObj.personInCharge = iUsrId; catObj.accFrom =
		 * new Integer("0");// another known bank account catObj.accTo =
		 * caObj.pkId; catObj.foreignTable = CashAccTxnBean.FT_CUST_RECEIPT;
		 * catObj.foreignKey = receiptObj.pkid; catObj.currency =
		 * naObj.currency; catObj.amount = bdPaymentAmt; catObj.txnTime =
		 * tsReceiptDate; catObj.remarks = strPaymentRmks; catObj.info1 = "";
		 * catObj.info2 = ""; catObj.state = CashAccTxnBean.ST_CREATED;
		 * catObj.status = CashAccTxnBean.STATUS_ACTIVE; catObj.lastUpdate =
		 * tsCreate; catObj.userIdUpdate = iUsrId; catObj.pcCenter =
		 * caObj.pcCenter; CashAccTxn catEJB = CashAccTxnNut.fnCreate(catObj);
		 * /// 5) update cash account balance // no need to set the balance !!!
		 * hurray !!!!
		 * 
		 * /// 3) update the nominal account NominalAccount naEJB =
		 * NominalAccountNut.getHandle(naObj.pkid); BigDecimal bdLatestBal =
		 * naEJB.getAmount(); bdLatestBal = bdLatestBal.add(natObj.amount);
		 * naObj.amount = bdLatestBal;
		 */
		return receiptObj.pkid;
	}

	private Long createCreditNote(Integer naPkid, String stmtType, String glCodeDebit, Timestamp stmtDate,
			String fStmtTable, Long fStmtKey, String refNo, String remarks, Timestamp timeUpdate, Integer userId,
			String currency, BigDecimal amount) throws Exception
	{
		// try
		// {
		String strErrMsg = null;
		/*
		 * Integer naPkid = new Integer(req.getParameter("naPkid")); String
		 * stmtType = req.getParameter("stmtType"); String glCodeDebit =
		 * req.getParameter("glCodeDebit"); Timestamp stmtDate =
		 * TimeFormat.createTimestamp( req.getParameter("stmtDate")); String
		 * fStmtTable = req.getParameter("fStmtTable"); Long fStmtKey = new
		 * Long(req.getParameter("fStmtKey")); String refNo =
		 * req.getParameter("refNo"); String remarks =
		 * req.getParameter("remarks"); Timestamp timeUpdate =
		 * TimeFormat.getTimestamp(); Integer userId =
		 * UserNut.getUserId(req.getParameter("userName")); BigDecimal amount =
		 * new BigDecimal(req.getParameter("amount"));
		 */
		// / checking input variables
		if (naPkid == null)
			throw new Exception(" Invalid Nominal Account PKID ");
		NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
		GenericStmtObject gsObj = new GenericStmtObject();
		gsObj.namespace = GenericStmtBean.NS_CUSTOMER;
		gsObj.dateStmt = stmtDate;
		// // assuming Credit Note here!!!!
		gsObj.glCodeDebit = glCodeDebit;
		gsObj.glCodeCredit = GLCodeBean.ACC_RECEIVABLE;
		// gsObj.currency = naObj.currency;
		gsObj.currency = currency; // don't inherit from NominalAccount
		gsObj.amount = amount;
		gsObj.dateDue = stmtDate;
		gsObj.foreignEntityTable = naObj.foreignTable;
		gsObj.foreignEntityKey = naObj.foreignKey;
		gsObj.pcCenter = naObj.accPCCenterId;
		gsObj.stmtType = stmtType;
		gsObj.foreignStmtTable = fStmtTable;
		gsObj.foreignStmtKey = fStmtKey;
		gsObj.referenceNo = refNo;
		// gsObj.remarks = remarks;
		gsObj.remarks = "AUTO-CN: " + remarks;
		gsObj.dateStart = gsObj.dateStmt;
		gsObj.dateEnd = gsObj.dateStmt;
		gsObj.dateDue = gsObj.dateStmt;
		gsObj.dateCreated = gsObj.dateStmt;
		gsObj.dateApproved = gsObj.dateStmt;
		gsObj.dateVerified = gsObj.dateStmt;
		gsObj.dateVerified = gsObj.dateStmt;
		gsObj.dateUpdate = timeUpdate;
		gsObj.userIdCreate = userId;
		gsObj.userIdPIC = userId;
		gsObj.userIdApprove = userId;
		gsObj.userIdVerified = userId;
		gsObj.userIdUpdate = userId;
		// create a nominal account txn for this generic stmt
		NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		natObj.nominalAccount = naObj.pkid;
		natObj.foreignTable = GenericStmtBean.TABLENAME;
		natObj.description = gsObj.remarks;
		natObj.glCodeDebit = gsObj.glCodeDebit;
		natObj.glCodeCredit = gsObj.glCodeCredit;
		natObj.currency = gsObj.currency;
		natObj.amount = gsObj.amount.negate();
		natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
		natObj.timeParam1 = gsObj.dateStmt;
		natObj.state = NominalAccountTxnBean.ST_ACTUAL;
		natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
		natObj.lastUpdate = TimeFormat.getTimestamp();
		natObj.userIdUpdate = gsObj.userIdCreate;
		GenericStmt gsEJB = GenericStmtNut.fnCreate(gsObj);
		if (gsEJB == null)
		{
			throw new Exception(" Unable to create Generic Stmt Bean ");
		}
		natObj.foreignKey = gsObj.pkid;
		NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
		if (natEJB == null)
		{
			try
			{
				gsEJB.remove();
			} catch (Exception ex)
			{
				ex.printStackTrace();
				throw new Exception(" Unable to create Nominal Account Txn ");
			}
		}
		naObj.amount = naObj.amount.add(natObj.amount);
		return gsObj.pkid;
	}

	private Long createDebitNote(Integer naPkid, String stmtType, String glCodeCredit, Timestamp stmtDate,
			String fStmtTable, Long fStmtKey, String refNo, String remarks, Timestamp timeUpdate, Integer userId,
			String currency, BigDecimal amount) throws Exception
	{
		String strErrMsg = null;
		// / checking input variables
		if (naPkid == null)
			throw new Exception(" Invalid Nominal Account PKID ");
		NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
		GenericStmtObject gsObj = new GenericStmtObject();
		gsObj.namespace = GenericStmtBean.NS_CUSTOMER;
		gsObj.dateStmt = stmtDate;
		// // assuming Credit Note here!!!!
		gsObj.glCodeDebit = GLCodeBean.ACC_RECEIVABLE;
		gsObj.glCodeCredit = glCodeCredit;
		// gsObj.currency = naObj.currency;
		gsObj.currency = currency;
		gsObj.amount = amount;
		gsObj.dateDue = stmtDate;
		gsObj.foreignEntityTable = naObj.foreignTable;
		gsObj.foreignEntityKey = naObj.foreignKey;
		gsObj.pcCenter = naObj.accPCCenterId;
		gsObj.stmtType = stmtType;
		gsObj.foreignStmtTable = fStmtTable;
		gsObj.foreignStmtKey = fStmtKey;
		gsObj.referenceNo = refNo;
		// gsObj.remarks = remarks;
		gsObj.remarks = "AUTO-DN: " + remarks;
		gsObj.dateStart = gsObj.dateStmt;
		gsObj.dateEnd = gsObj.dateStmt;
		gsObj.dateDue = gsObj.dateStmt;
		gsObj.dateCreated = gsObj.dateStmt;
		gsObj.dateApproved = gsObj.dateStmt;
		gsObj.dateVerified = gsObj.dateStmt;
		gsObj.dateVerified = gsObj.dateStmt;
		gsObj.dateUpdate = timeUpdate;
		gsObj.userIdCreate = userId;
		gsObj.userIdPIC = userId;
		gsObj.userIdApprove = userId;
		gsObj.userIdVerified = userId;
		gsObj.userIdUpdate = userId;
		// create a nominal account txn for this generic stmt
		NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		natObj.nominalAccount = naObj.pkid;
		natObj.foreignTable = GenericStmtBean.TABLENAME;
		natObj.description = gsObj.remarks;
		natObj.glCodeDebit = gsObj.glCodeDebit;
		natObj.glCodeCredit = gsObj.glCodeCredit;
		natObj.currency = gsObj.currency;
		// natObj.amount = gsObj.amount.negate();
		natObj.amount = gsObj.amount;
		natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
		natObj.timeParam1 = gsObj.dateStmt;
		natObj.state = NominalAccountTxnBean.ST_ACTUAL;
		natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
		natObj.lastUpdate = TimeFormat.getTimestamp();
		natObj.userIdUpdate = gsObj.userIdCreate;
		GenericStmt gsEJB = GenericStmtNut.fnCreate(gsObj);
		if (gsEJB == null)
		{
			throw new Exception(" Unable to create Generic Stmt Bean ");
		}
		natObj.foreignKey = gsObj.pkid;
		NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
		if (natEJB == null)
		{
			try
			{
				gsEJB.remove();
			} catch (Exception ex)
			{
				ex.printStackTrace();
				throw new Exception(" Unable to create Nominal Account Txn ");
			}
		}
		naObj.amount = naObj.amount.add(natObj.amount);
		return gsObj.pkid;
	}

	private Long createReversePayment(String strStmtType, Integer iNominalAccId, Integer iCashAccountId,
			String currency, BigDecimal bdPaymentAmt, String strChequeCreditCardNo, String strRemarks, String strInfo1,
			Timestamp tsDateStmt, String strForeignStmtTable, Long iSettleStmtId, Integer usrid) throws Exception
	{
		String strErrMsg = null;
		// first of all, get the for parameters
		// String strStmtType = (String) req.getParameter("stmtType");
		// String strNominalAcc = (String)req.getParameter("nominalAcc");
		// String strCashAccount = (String)req.getParameter("cashAccount");
		// String strAmount = (String)req.getParameter("amount");
		// BigDecimal bdPaymentAmt= new BigDecimal(strAmount);
		// String strAmtInWords = (String)req.getParameter("amtInWords");
		String strAmtInWords = CurrencyFormat.toWords(bdPaymentAmt, "", "");
		strAmtInWords = strAmtInWords + " ONLY";
		/*
		 * String strChequeCreditCardNo = (String)
		 * req.getParameter("chequeCreditCardNo"); String strRemarks =
		 * (String)req.getParameter("remarks"); String strInfo1 =
		 * (String)req.getParameter("info1");
		 * 
		 * String strDateStmt = (String) req.getParameter("dateStmt");
		 * 
		 * String foreignStmtTable = (String)
		 * req.getParameter("foreignStmtTable"); String strSettleStmtId =
		 * (String) req.getParameter("foreignStmtKey"); String userName =
		 * (String) req.getParameter("userName");
		 */
		// Get the cash account object
		CashAccountObject caObj = null;
		try
		{
			caObj = CashAccountNut.getObject(
			// new Integer(strCashAccount));
					iCashAccountId);
		} catch (Exception ex)
		{ /* do nothing */
		}
		if (caObj == null)
		{
			throw new Exception("Invalid Cash Account Object ");
		}
		// Get the nominal account object
		NominalAccountObject naObj = null;
		try
		{
			naObj = NominalAccountNut.getObject(
			// new Integer(strNominalAcc));
					iNominalAccId);
		} catch (Exception ex)
		{ /* do nothing */
		}
		if (naObj == null)
		{
			throw new Exception("Error fetching Nominal Account Object ");
		}
		GenericStmtObject gsObj = new GenericStmtObject();
		try
		{
			gsObj.namespace = GenericStmtBean.NS_CUSTOMER;
			gsObj.pcCenter = naObj.accPCCenterId;
			gsObj.glCodeDebit = GLCodeBean.ACC_RECEIVABLE;
			gsObj.glCodeCredit = caObj.accountType;
			// gsObj.currency = naObj.currency;
			gsObj.currency = currency;
			gsObj.amount = bdPaymentAmt;
			gsObj.cashFrom = caObj.pkId;
			gsObj.cashTo = caObj.pkId;
			gsObj.stmtType = strStmtType;
			// gsObj.foreignStmtTable = GenericStmtBean.TABLENAME;
			gsObj.foreignStmtTable = strForeignStmtTable;
			gsObj.foreignStmtKey = iSettleStmtId;
			gsObj.chequeCreditCardNo = strChequeCreditCardNo;
			gsObj.remarks = strRemarks;
			gsObj.info1 = strInfo1;
			gsObj.info2 = strAmtInWords;
			gsObj.nominalAccount = naObj.pkid;
			gsObj.foreignEntityTable = GenericEntityAccountBean.TABLENAME;
			gsObj.foreignEntityKey = naObj.foreignKey;
			gsObj.dateStmt = tsDateStmt;
			gsObj.dateStart = tsDateStmt;
			gsObj.dateEnd = tsDateStmt;
			gsObj.dateDue = tsDateStmt;
			gsObj.dateCreated = TimeFormat.getTimestamp();
			gsObj.dateApproved = TimeFormat.getTimestamp();
			gsObj.dateVerified = TimeFormat.getTimestamp();
			gsObj.dateUpdate = TimeFormat.getTimestamp();
			gsObj.userIdCreate = usrid;
			gsObj.userIdPIC = gsObj.userIdCreate;
			gsObj.userIdApprove = gsObj.userIdCreate;
			gsObj.userIdVerified = gsObj.userIdCreate;
			gsObj.userIdUpdate = gsObj.userIdCreate;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception(" Unable to create GenericStmtObject ");
		}
		// update the cash account txn
		CashAccTxnObject catObjF = new CashAccTxnObject();
		catObjF.txnCode = CashAccTxnBean.TXN_CODE_DEFAULT;
		catObjF.glCodeDebit = gsObj.glCodeDebit;
		catObjF.glCodeCredit = gsObj.glCodeCredit;
		catObjF.personInCharge = gsObj.userIdCreate;
		// catObjF.accFrom = caObjTo.pkId;
		catObjF.accTo = caObj.pkId;
		catObjF.foreignTable = CashAccTxnBean.FT_GENERIC_STMT;
		catObjF.foreignKey = gsObj.pkid;
		catObjF.currency = gsObj.currency;
		catObjF.amount = gsObj.amount.negate();
		catObjF.txnTime = gsObj.dateStmt;
		catObjF.remarks = gsObj.remarks;
		catObjF.state = CashAccTxnBean.ST_CREATED;
		catObjF.status = CashAccTxnBean.STATUS_ACTIVE;
		catObjF.lastUpdate = TimeFormat.getTimestamp();
		catObjF.userIdUpdate = gsObj.userIdCreate;
		catObjF.pcCenter = caObj.pcCenter;
		CashAccTxnNut.fnCreate(catObjF);
		// create a nominal account txn for this generic stmt
		NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		natObj.nominalAccount = naObj.pkid;
		natObj.foreignTable = GenericStmtBean.TABLENAME;
		// the natObj.foreignKey will be assigned later after gsObj
		// is created and a pkid is obtained
		natObj.description = gsObj.remarks;
		natObj.glCodeDebit = gsObj.glCodeDebit;
		natObj.glCodeCredit = gsObj.glCodeCredit;
		natObj.currency = gsObj.currency;
		natObj.amount = gsObj.amount;
		natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
		natObj.timeParam1 = gsObj.dateStmt;
		natObj.state = NominalAccountTxnBean.ST_ACTUAL;
		natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
		natObj.lastUpdate = TimeFormat.getTimestamp();
		natObj.userIdUpdate = gsObj.userIdCreate;
		GenericStmt gsEJB = GenericStmtNut.fnCreate(gsObj);
		if (gsEJB == null)
		{
			throw new Exception(" Unable to create Generic Stmt Bean ");
		}
		natObj.foreignKey = gsObj.pkid;
		NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
		if (natEJB == null)
		{
			try
			{
				gsEJB.remove();
			} catch (Exception ex)
			{
				ex.printStackTrace();
				throw new Exception(" Unable to create Nominal Account Txn ");
			}
		}
		naObj.amount = naObj.amount.add(natObj.amount);
		return gsObj.pkid;
	}

	private Vector getPaymentSum(Connection con, String payNo, String strCurr, String remarks, Integer usrid)
			throws Exception
	{
		// drop tmp table
		try
		{
			String dropTmpTable = "drop table tmp_payrefd";
			Statement dropTmpTableStmt = con.createStatement();
			dropTmpTableStmt.executeUpdate(dropTmpTable);
		} catch (Exception ex)
		{
			// ignore
		}
		String selectDistinct = "select distinct payno, txrefno, custid, lpayamt into tmp_payrefd "
				+ "from payrefd where payno = '" + payNo + "'";
		Log.printVerbose("selectDistinct = " + selectDistinct);
		String getPymtSumQ = "select custid, sum(lpayamt) from tmp_payrefd group by custid";
		Log.printVerbose("getPymtSumQ = " + getPymtSumQ);
		String strCustId = null;
		BigDecimal bdSumOfPayments = new BigDecimal(0);
		Vector vecRtn = new Vector();
		Statement selectDistinctStmt = con.createStatement();
		Statement getPymtSumStmt = con.createStatement();
		selectDistinctStmt.executeUpdate(selectDistinct);
		ResultSet rsGetPymtSum = getPymtSumStmt.executeQuery(getPymtSumQ);
		while (rsGetPymtSum.next())
		{
			Log.printVerbose("Found a hit ...");
			// try
			// {
			strCustId = rsGetPymtSum.getString(1);
			bdSumOfPayments = rsGetPymtSum.getBigDecimal(2);
			// derive the NominalAccount
			CustAccount thisCustAcc = CustAccountNut.getObjectByCode(strCustId);
			NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER, iDefPCCenterId,
					thisCustAcc.getPkid(), strCurr);
			if (naObj == null)
			{
				naObj = new NominalAccountObject();
				// naObj.pkid = new Integer("0");
				// naObj.code = new String("not_used");
				naObj.namespace = NominalAccountBean.NS_CUSTOMER;
				naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
				naObj.foreignKey = thisCustAcc.getPkid();
				naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
				naObj.currency = strCurr;
				// naObj.amount = naObj.amount.add(lTxAmt);
				naObj.remarks = remarks;
				naObj.accPCCenterId = iDefPCCenterId;
				naObj.state = NominalAccountBean.STATE_CREATED;
				naObj.status = NominalAccountBean.STATUS_ACTIVE;
				naObj.lastUpdate = TimeFormat.getTimestamp();
				naObj.userIdUpdate = usrid;
				NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
			}
			vecRtn.add(new Payment(naObj.pkid, bdSumOfPayments));
			// }
			// catch(Exception ex)
			// {
			// }
		} // end while
		selectDistinctStmt.close();
		getPymtSumStmt.close();
		return vecRtn;
	}

	private Vector getRevPaymentSum(Connection con, String docNo, String strCurr, String remarks, Integer usrid)
			throws Exception
	{
		String getPymtSumQ = "select custid, sum(lpayamt) from "
				+ "(select distinct payno, txrefno, custid, lpayamt from payrefd " + " where txrefno = '" + docNo
				+ "') as tmp_payrefd group by custid";
		Log.printVerbose("getRevPymtSumQ = " + getPymtSumQ);
		String strCustId = null;
		BigDecimal bdSumOfPayments = new BigDecimal(0);
		Vector vecRtn = new Vector();
		Statement getPymtSumStmt = con.createStatement();
		ResultSet rsGetPymtSum = getPymtSumStmt.executeQuery(getPymtSumQ);
		while (rsGetPymtSum.next())
		{
			Log.printVerbose("Found a hit ...");
			// try
			// {
			strCustId = rsGetPymtSum.getString(1);
			bdSumOfPayments = rsGetPymtSum.getBigDecimal(2);
			// derive the NominalAccount
			CustAccount thisCustAcc = CustAccountNut.getObjectByCode(strCustId);
			NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER, iDefPCCenterId,
					thisCustAcc.getPkid(), strCurr);
			if (naObj == null)
			{
				naObj = new NominalAccountObject();
				// naObj.pkid = new Integer("0");
				// naObj.code = new String("not_used");
				naObj.namespace = NominalAccountBean.NS_CUSTOMER;
				naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
				naObj.foreignKey = thisCustAcc.getPkid();
				naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
				naObj.currency = strCurr;
				// naObj.amount = naObj.amount.add(lTxAmt);
				naObj.remarks = remarks;
				naObj.accPCCenterId = iDefPCCenterId;
				naObj.state = NominalAccountBean.STATE_CREATED;
				naObj.status = NominalAccountBean.STATUS_ACTIVE;
				naObj.lastUpdate = TimeFormat.getTimestamp();
				naObj.userIdUpdate = usrid;
				NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
			}
			vecRtn.add(new Payment(naObj.pkid, bdSumOfPayments));
			// }
			// catch(Exception ex)
			// {
			// }
		} // end while
		getPymtSumStmt.close();
		return vecRtn;
	}

	/*
	 * private BigDecimal getPaymentSum(Connection con, String payNo) throws
	 * Exception { String getPymtSumQ = "select sum(lpayamt) from payrefd where
	 * payno = '" + payNo + "'";
	 * 
	 * BigDecimal bdSumOfPayments = new BigDecimal(0); Statement getPymtSumStmt =
	 * con.createStatement(); ResultSet rsGetPymtSum =
	 * getPymtSumStmt.executeQuery(getPymtSumQ); if (rsGetPymtSum.next()) { try {
	 * bdSumOfPayments = rsGetPymtSum.getBigDecimal(1); } catch(Exception ex) { }
	 * 
	 * if(bdSumOfPayments == null) bdSumOfPayments = new BigDecimal(0); }
	 * 
	 * return bdSumOfPayments; }
	 */
	/*
	 * private BigDecimal getRevPaymentSum(Connection con, String payNo) throws
	 * Exception { String getPymtSumQ = "select sum(lpayamt) from payrefd where
	 * txrefno = '" + payNo + "'";
	 * 
	 * BigDecimal bdSumOfPayments = new BigDecimal(0); Statement getPymtSumStmt =
	 * con.createStatement(); ResultSet rsGetPymtSum =
	 * getPymtSumStmt.executeQuery(getPymtSumQ); if (rsGetPymtSum.next()) { try {
	 * bdSumOfPayments = rsGetPymtSum.getBigDecimal(1); } catch(Exception ex) { }
	 * 
	 * if(bdSumOfPayments == null) bdSumOfPayments = new BigDecimal(0); }
	 * 
	 * return bdSumOfPayments; }
	 */
	private void fixExRate(Connection topconCon, Connection jbossCon, Integer usrid) throws Exception
	{
		String findInvQ = "select pkid from cust_invoice_index where remarks ~* ? ";
		PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		String findCurrQ = "select currid from cdhis where docno = ? ";
		PreparedStatement findCurrStmt = topconCon.prepareStatement(findCurrQ);
		String queryXRateGainLose = "select * from payrefd where gainloseamt != 0 ";
		Statement xRateGainLoseStmt = topconCon.createStatement();
		ResultSet rsXRateGainLose = xRateGainLoseStmt.executeQuery(queryXRateGainLose);
		int count = 0;
		while (rsXRateGainLose.next())
		{
			Log.printVerbose("*** Processing Txn " + ++count);
			String strPayNo = rsXRateGainLose.getString("payno");
			String strTxRefNo = rsXRateGainLose.getString("txrefno");
			String strCustCode = rsXRateGainLose.getString("custid");
			Timestamp payDate = TimeFormat.createTimeStamp(rsXRateGainLose.getString("paydate"), "MM/dd/yy HH:mm:ss");
			BigDecimal bdPayExchRate = rsXRateGainLose.getBigDecimal("exch_rate");
			BigDecimal bdTxExchRate = rsXRateGainLose.getBigDecimal("txexch_rate");
			BigDecimal bdGainLoseAmt = rsXRateGainLose.getBigDecimal("gainloseamt");
			// Get the Invoice Id
			Long invId = new Long(0);
			findInvStmt.setString(1, " = " + strTxRefNo + ")");
			ResultSet rsFindInv = findInvStmt.executeQuery();
			if (rsFindInv.next())
			{
				invId = new Long(rsFindInv.getLong("pkid"));
			}
			// Get the Customer Id
			Integer custId = null;
			//findCustStmt.setString(1, strCustCode);
			findCustStmt.setString(1, strCustCode);
			ResultSet rsFindCust = findCustStmt.executeQuery();
			if (rsFindCust.next())
			{
				custId = new Integer(rsFindCust.getInt("pkid"));
			}
			// Get the Currency
			String strCurr = null;
			findCurrStmt.setString(1, strPayNo);
			ResultSet rsFindCurr = findCurrStmt.executeQuery();
			if (rsFindCurr.next())
			{
				strCurr = rsFindCurr.getString("currid");
				strCurr = (String) hmCurr.get(new Integer(strCurr));
			}
			// Get the Nominal Account
			NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER, iDefPCCenterId,
					custId, "MYR");
			if (naObj == null)
			{
				naObj = new NominalAccountObject();
				// naObj.pkid = new Integer("0");
				// naObj.code = new String("not_used");
				naObj.namespace = NominalAccountBean.NS_CUSTOMER;
				naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
				naObj.foreignKey = custId;
				naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
				// naObj.currency = thisCurr;
				naObj.currency = "MYR";
				// naObj.amount = naObj.amount.add(lTxAmt);
				// naObj.remarks = remarks;
				naObj.accPCCenterId = iDefPCCenterId;
				naObj.state = NominalAccountBean.STATE_CREATED;
				naObj.status = NominalAccountBean.STATUS_ACTIVE;
				naObj.lastUpdate = TimeFormat.getTimestamp();
				naObj.userIdUpdate = usrid;
				NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
			}
			String strRemarks = "EXCH_RATE OFFSET (txexch_rate=" + bdTxExchRate.toString() + ", pay_exch_rate="
					+ bdPayExchRate.toString() + ")";
			// if bdGainLoseAmt < 0, means we need to CN to offset the balance
			// if bdGainLoseAmt > 0, means we need to DN to offset the balance
			if (bdGainLoseAmt.signum() < 0)
			{
				Log.printVerbose("*** " + count + ": Creating CN");
				Long newCNId = createCreditNote(naObj.pkid, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
						GLCodeBean.CASH_DISCOUNT, payDate, InvoiceBean.TABLENAME, invId, strPayNo, strRemarks,
						TimeFormat.getTimestamp(), usrid, strCurr, bdGainLoseAmt.abs());
			} else
			{
				// Create a Debit Note
				Log.printVerbose("*** " + count + ": Creating DN");
				Long newDNId = createDebitNote(naObj.pkid, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
						GLCodeBean.INTEREST_REVENUE, payDate, InvoiceBean.TABLENAME, invId, strPayNo, strRemarks,
						TimeFormat.getTimestamp(), usrid, strCurr, bdGainLoseAmt.abs());
			}
		} // end while
	} // end fixExRate
	// internal class
	class Payment
	{
		public BigDecimal amt;
		public Integer nomAccId;

		Payment(Integer nomAccId, BigDecimal amt)
		{
			this.amt = amt;
			this.nomAccId = nomAccId;
		}
	}
}
package com.vlee.servlet.test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vlee.ejb.accounting.BranchNut;
import com.vlee.ejb.accounting.BranchObject;
import com.vlee.ejb.accounting.CashAccTxnBean;
import com.vlee.ejb.accounting.CashAccTxnNut;
import com.vlee.ejb.accounting.CashAccTxnObject;
import com.vlee.ejb.accounting.CashAccountNut;
import com.vlee.ejb.accounting.CashAccountObject;
import com.vlee.ejb.accounting.GLCodeBean;
import com.vlee.ejb.accounting.GenericEntityAccountBean;
import com.vlee.ejb.accounting.GenericStmt;
import com.vlee.ejb.accounting.GenericStmtBean;
import com.vlee.ejb.accounting.GenericStmtNut;
import com.vlee.ejb.accounting.GenericStmtObject;
import com.vlee.ejb.accounting.NominalAccount;
import com.vlee.ejb.accounting.NominalAccountBean;
import com.vlee.ejb.accounting.NominalAccountNut;
import com.vlee.ejb.accounting.NominalAccountObject;
import com.vlee.ejb.accounting.NominalAccountTxn;
import com.vlee.ejb.accounting.NominalAccountTxnBean;
import com.vlee.ejb.accounting.NominalAccountTxnNut;
import com.vlee.ejb.accounting.NominalAccountTxnObject;
import com.vlee.ejb.accounting.OfficialReceipt;
import com.vlee.ejb.accounting.OfficialReceiptNut;
import com.vlee.ejb.accounting.OfficialReceiptObject;
import com.vlee.ejb.customer.CustAccount;
import com.vlee.ejb.customer.CustAccountBean;
import com.vlee.ejb.customer.CustAccountNut;
import com.vlee.ejb.customer.CustServiceCenterNut;
import com.vlee.ejb.customer.CustServiceCenterObject;
import com.vlee.ejb.customer.Invoice;
import com.vlee.ejb.customer.InvoiceBean;
import com.vlee.ejb.customer.InvoiceItem;
import com.vlee.ejb.customer.InvoiceItemNut;
import com.vlee.ejb.customer.InvoiceItemObject;
import com.vlee.ejb.customer.InvoiceNut;
import com.vlee.ejb.customer.InvoiceObject;
import com.vlee.ejb.customer.POSItem;
import com.vlee.ejb.customer.POSItemBean;
import com.vlee.ejb.customer.POSItemChildObject;
import com.vlee.ejb.customer.POSItemNut;
import com.vlee.ejb.customer.POSItemObject;
import com.vlee.ejb.inventory.Item;
import com.vlee.ejb.inventory.ItemBean;
import com.vlee.ejb.inventory.ItemNut;
import com.vlee.ejb.inventory.ItemObject;
import com.vlee.ejb.inventory.Location;
import com.vlee.ejb.inventory.LocationNut;
import com.vlee.ejb.inventory.Stock;
import com.vlee.ejb.inventory.StockNut;
import com.vlee.ejb.inventory.StockObject;
import com.vlee.ejb.user.RoleBean;
import com.vlee.ejb.user.User;
import com.vlee.ejb.user.UserDetails;
import com.vlee.ejb.user.UserDetailsNut;
import com.vlee.ejb.user.UserNut;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionDo;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.CurrencyFormat;
import com.vlee.util.Log;
import com.vlee.util.Task;
import com.vlee.util.TimeFormat;

public class DoMigrateTopConDB implements Action
{
	private String strClassName = "DoMigrateTopConDB";
	private static Task curTask = null;
	// Constants
	private static Integer iDefLocId = new Integer(1000);
	private static Integer iDefSvcCtrId = new Integer(1);
	private static Integer iDefPCCenterId = new Integer(1000);
	private static Integer iDefCondId = new Integer(StockNut.STK_COND_GOOD);
	private static Integer iDefCashAccId = new Integer(1000);
	private static Integer iDefChequeAccId = new Integer(1001);
	private BranchObject defBranchObj = null;
	private static HashMap hmCurr = new HashMap();
	static
	{
		hmCurr.put(new Integer(1), "MYR");
		hmCurr.put(new Integer(2), "USD");
		hmCurr.put(new Integer(3), "SGD");
		hmCurr.put(new Integer(4), "4");
		hmCurr.put(new Integer(5), "5");
		hmCurr.put(new Integer(6), "6");
		hmCurr.put(new Integer(7), "7");
	}

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
			/*******************************************************************
			 * // 1. Extract BranchInfo
			 ******************************************************************/
//			String query = "select * from branchinfo";
//			Statement stmt = con.createStatement();
//			ResultSet rs = stmt.executeQuery(query);
//			curTask = new Task("Extract BranchInfo", rs.getFetchSize());
//			while (rs.next())
//			{
//				curTask.increment();
//				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
//				Log.printVerbose("Got one ... ");
//				String coNo = rs.getString("cono");
//				String branchName = rs.getString("branchname");
//				String coname = rs.getString("coname");
//				Log.printVerbose("coNo = " + coNo);
//				Log.printVerbose("branchName = " + branchName);
//				Log.printVerbose("coname = " + coname);
//				// insert to location, cust_svc_center, supp_svc_center
//				// Assumes locaddr has a default row of pkid = 0
//				// NEW: Discovered that they are 2 Locid ("00","99")
//				Location newLoc = LocationNut.getObjectByCode("00");
//				if (newLoc == null)
//				{
//					newLoc = LocationNut.getHome().create(LocationBean.NS_TRADING, "", "00", branchName + " (00)",
//							coname, iDefPCCenterId, new Integer(0), TimeFormat.getTimestamp(), usrid);
//				}
//				Location newLoc2 = LocationNut.getObjectByCode("99");
//				if (newLoc2 == null)
//				{
//					newLoc2 = LocationNut.getHome().create(LocationBean.NS_TRADING, "", "99", branchName + " (99)",
//							coname, iDefPCCenterId, new Integer(0), TimeFormat.getTimestamp(), usrid);
//				}
//				Branch newBranch = BranchNut.getObjectByCode(coNo);
//				if (newBranch == null)
//				{
//					BranchObject newBranchObj = new BranchObject();
//					newBranchObj.code = coNo;
//					// newBranchObj.regNo = "";
//					newBranchObj.name = branchName;
//					newBranchObj.description = coname;
//					// newBranchObj.addr1 = req.getParameter("addr1");
//					// newBranchObj.addr2 = req.getParameter("addr2");
//					// newBranchObj.addr3 = req.getParameter("addr3");
//					// newBranchObj.zip = req.getParameter("zip");
//					// newBranchObj.state = req.getParameter("state");
//					// newBranchObj.countryCode =
//					// req.getParameter("countryCode");
//					// newBranchObj.phoneNo = req.getParameter("phoneNo");
//					// newBranchObj.faxNo = req.getParameter("faxNo");
//					// newBranchObj.webUrl = req.getParameter("webUrl");
//					newBranchObj.accPCCenterId = iDefPCCenterId;
//					newBranchObj.invLocationId = iDefLocId;
//					// newBranchObj.cashbookCash = new
//					// Integer(req.getParameter("cashbookCash"));
//					// newBranchObj.cashbookCard = new
//					// Integer(req.getParameter("cashbookCard"));
//					// newBranchObj.cashbookCheque = new
//					// Integer(req.getParameter("cashbookCheque"));
//					// newBranchObj.cashbookPDCheque = new
//					// Integer(req.getParameter("cashbookPDCheque"));
//					// newBranchObj.currency = req.getParameter("currency");
//					// newBranchObj.pricing = req.getParameter("pricing");
//					// newBranchObj.hotlines = req.getParameter("hotlines");
//					// newBranchObj.logoURL= req.getParameter("logoURL");
//					newBranch = BranchNut.fnCreate(newBranchObj);
//				}
//			}
//			stmt.close();
			/*******************************************************************
			 * // 2. Extract Customer Info
			 ******************************************************************/
//			String custQuery = "select * from cus";
//			Statement custStmt = con.createStatement();
//			ResultSet rsCust = custStmt.executeQuery(custQuery);
//			// String cleanCust = "delete from cust_account_index where pkid !=
//			// 1";
//			// Statement cleanStmt = jbossCon.createStatement();
//			curTask = new Task("Extract Customer", rsCust.getFetchSize());
//			while (rsCust.next())
//			{
//				curTask.increment();
//				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
//				Log.printVerbose("Got a customer ... ");
//				String custId = rsCust.getString("custid");
//				String shortName = rsCust.getString("shortname");
//				String name1 = rsCust.getString("name1");
//				String custType = rsCust.getString("custtype");
//				String contact = rsCust.getString("contact");
//				String addr1 = rsCust.getString("addr1");
//				String addr2 = rsCust.getString("addr2");
//				String addr3 = rsCust.getString("addr3");
//				String tel = rsCust.getString("tel");
//				String fax = rsCust.getString("fax");
//				// Create the custAccount
//				CustAccount newCustAcc = CustAccountNut.getObjectByCode(custId);
//				if (newCustAcc == null)
//				{
//					CustAccountObject custObj = new CustAccountObject();
//					custObj.name = shortName;
//					custObj.custAccountCode = custId;
//					custObj.description = name1;
//					custObj.nameFirst = contact;
//					// custObj.accType = CustAccountBean.ACCTYPE_NORMAL_ENUM;
//					custObj.mainAddress1 = addr1;
//					custObj.mainAddress2 = addr2;
//					custObj.mainAddress3 = addr3;
//					custObj.telephone1 = tel;
//					custObj.faxNo = fax;
//					newCustAcc = CustAccountNut.fnCreate(custObj);
//					if (newCustAcc == null)
//						throw new Exception("Failed to create CustAccount " + custId);
//				}
//			}
//			custStmt.close();
			/*******************************************************************
			 * // 2a. Extract SalesRep Info
			 ******************************************************************/
//			String salesRepQuery = "select * from srep";
//			Statement salesRepStmt = con.createStatement();
//			ResultSet rsSalesRep = salesRepStmt.executeQuery(salesRepQuery);
//			curTask = new Task("Extract SalesRep", rsSalesRep.getFetchSize());
//			while (rsSalesRep.next())
//			{
//				curTask.increment();
//				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
//				String srId = rsSalesRep.getString("srid");
//				String srDesc = rsSalesRep.getString("description");
//				createNewSRep(srId, srDesc);
//			} // end rsSalesRep
			/*******************************************************************
			 * // 3. Extract Stock Info
			 ******************************************************************/
//			// Create a DUMMY Non-Inventory Item to track all stocks that have
//			// missing or unaccounted stkId
//			Item nonInvItem = ItemNut.getObjectByCode("non-inv");
//			if (nonInvItem == null)
//			{
//				ItemObject itemObj = new ItemObject();
//				// populate the properties here!!
//				itemObj.code = "non-inv";
//				itemObj.name = "Non-Inventory";
//				itemObj.description = "General Non-Inventory";
//				itemObj.userIdUpdate = usrid;
//				itemObj.enumInvType = new Integer(ItemBean.INV_TYPE_NONSTK);
//				nonInvItem = ItemNut.fnCreate(itemObj);
//			}
//			// Create a corresponding NON_INV PosItem
//			POSItem nonInvPOSItem = POSItemNut.getPOSItem(nonInvItem.getPkid(), POSItemBean.TYPE_NINV);
//			if (nonInvPOSItem == null)
//			{
//				POSItemObject newNIPosItemObj = new POSItemObject();
//				newNIPosItemObj.itemFKId = nonInvItem.getPkid();
//				newNIPosItemObj.itemType = POSItemBean.TYPE_NINV;
//				newNIPosItemObj.currency = "MYR";
//				// newNIPosItemObj.unitPriceStd = new BigDecimal("0.00");
//				// newNIPosItemObj.unitPriceDiscounted = new BigDecimal("0.00");
//				// newNIPosItemObj.unitPriceMin = new BigDecimal("0.00");
//				// newNIPosItemObj.timeEffective = TimeFormat.getTimestamp();
//				newNIPosItemObj.status = POSItemBean.STATUS_ACTIVE;
//				// newNIPosItemObj.lastUpdate = TimeFormat.getTimestamp();
//				newNIPosItemObj.userIdUpdate = usrid;
//				// newNIPosItemObj.costOfItem = new BigDecimal("0.00");
//				nonInvPOSItem = POSItemNut.fnCreate(newNIPosItemObj);
//			}
//			String stkQuery = "select * from stkmaster";
//			Statement stkStmt = con.createStatement();
//			ResultSet rsStk = stkStmt.executeQuery(stkQuery);
//			// String cleanCust = "delete from cust_account_index where pkid !=
//			// 1";
//			// Statement cleanStmt = jbossCon.createStatement();
//			curTask = new Task("Extract Stock", rsStk.getFetchSize());
//			while (rsStk.next())
//			{
//				curTask.increment();
//				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
//				String stkId = rsStk.getString("stkid");
//				String shortName = rsStk.getString("shortname");
//				String desc1 = rsStk.getString("desc1");
//				String desc2 = rsStk.getString("desc2");
//				String desc3 = rsStk.getString("desc3");
//				BigDecimal aveUCost = rsStk.getBigDecimal("aveucost");
//				BigDecimal rplUCost = rsStk.getBigDecimal("rplucost");
//				BigDecimal curBalQty = rsStk.getBigDecimal("curbalqty");
//				String catid = rsStk.getString("catid");// / map to category1 of
//														// inv_item
//				String deptid = rsStk.getString("deptid");// / map to
//															// category2 of
//															// inv_item
//				String grpid = rsStk.getString("grpid");// / map to category3 of
//														// inv_item
//				// Add the item index if doesn't already exist
//				Item newItem = ItemNut.getObjectByCode(stkId);
//				if (newItem == null)
//				{
//					String uomQuery = "select distinct uom from cdhisd where stkid = '" + stkId + "'";
//					Statement uomStmt = con.createStatement();
//					ResultSet rsUOM = uomStmt.executeQuery(uomQuery);
//					Integer thisUOM = new Integer(ItemBean.UOM_NONE);
//					ItemObject itemObj = new ItemObject();
//					if (rsUOM.next())
//					{
//						String uom = rsUOM.getString("uom");
//						itemObj.uom = uom;
//						if (uom.equals("PCE") || uom.equals("PCS"))
//							thisUOM = new Integer(ItemBean.UOM_PCS);
//						else if (uom.equals("ROLL"))
//							thisUOM = new Integer(ItemBean.UOM_ROLL);
//						else if (uom.equals("SET") || uom.equals("SETS") || uom.equals("SSET"))
//							thisUOM = new Integer(ItemBean.UOM_SET);
//						else if (uom.equals("UNIT") || uom.equals("NOS"))
//							thisUOM = new Integer(ItemBean.UOM_UNIT);
//					}
//					// populate the properties here!!
//					itemObj.code = stkId;
//					itemObj.name = shortName;
//					itemObj.description = desc1 + desc2 + desc3;
//					itemObj.userIdUpdate = usrid;
//					itemObj.enumInvType = new Integer(ItemBean.INV_TYPE_INVENTORY);
//					itemObj.categoryId = new Integer(1000);
//					itemObj.unitOfMeasure = thisUOM;
//					itemObj.category1 = catid;
//					itemObj.category2 = deptid;
//					itemObj.category3 = grpid;
//					newItem = ItemNut.fnCreate(itemObj);
//				}
//				// Then populate the POSITems
//				POSItem newPOSItem = POSItemNut.getPOSItem(newItem.getPkid(), POSItemBean.TYPE_INV);
//				if (newPOSItem == null)
//				{
//					POSItemObject newPOSItemObj = new POSItemObject();
//					newPOSItemObj.itemFKId = newItem.getPkid();
//					newPOSItemObj.itemType = POSItemBean.TYPE_INV;
//					newPOSItemObj.currency = "MYR";
//					newPOSItemObj.unitPriceStd = rplUCost;
//					// newPOSItemObj.unitPriceDiscounted = new
//					// BigDecimal("0.00");
//					// newPOSItemObj.unitPriceMin = new BigDecimal("0.00");
//					// newPOSItemObj.timeEffective = TimeFormat.getTimestamp();
//					newPOSItemObj.status = POSItemBean.STATUS_ACTIVE;
//					// newPOSItemObj.lastUpdate = TimeFormat.getTimestamp();
//					newPOSItemObj.userIdUpdate = usrid;
//					// newPOSItemObj.costOfItem = new BigDecimal("0.00");
//					newPOSItem = POSItemNut.fnCreate(newPOSItemObj);
//				}
//				// Then populate the corresponding stock table (only 1 row for
//				// each item since we only have one warehouse
//				Stock newStk = StockNut.getObjectBy(newItem.getPkid(), iDefLocId, iDefCondId, "");
//				if (newStk == null)
//				{
//					StockObject newObj = new StockObject();
//					newObj.itemId = newItem.getPkid();
//					newObj.locationId = iDefLocId;
//					newObj.accPCCenterId = iDefPCCenterId;
//					newObj.userIdUpdate = usrid;
//					newStk = StockNut.fnCreate(newObj);
//				}
//			}
//			stkStmt.close();
			/*******************************************************************
			 * // 4. Extract Sales Txn
			 ******************************************************************/
			/*
			 * a. Sort the CDHis by TxDate b. For each row, switch(txType) if
			 * (I) // for invoice Create a SalesTxn from the top down to Nominal
			 * Account, in this manner: SalesOrder -> SOItems -> DO -> DOItems ->
			 * Invoice -> InvoiceItems -> Nominal Account
			 * 
			 * The soItems/invoiceItems taken from CDHisD table (foreign key =
			 * DocRef)
			 * 
			 * if (P) // for Payment Find each invoice mapped to the payment
			 * (foreign key = docRef) If > 1 mapping, i.e. payment is used to
			 * pay > 1 invoice, put that info in the remarks of the receipt for
			 * the time being, If 1 mapping, put that into SalesTxnId (TO_DO:
			 * Waiting for the acc_settlement table to be out) Update the
			 * NominalAccount accordingly (should be done inside Receipt)
			 * 
			 * if (C) // Credit Note Create new Credit Note (using Generic Stmt)
			 * 
			 * if (D) // Debit Note Create new Debit Note (using Generic Stmt) //
			 * may not be ready yet, but leave this out first
			 * 
			 * if (R) // Reverse Payment, used to bring the Invoice back to its
			 * original state, // e.g. "Acct Closed", "Credit Card Refund
			 * Reversed or", "Nsf", "Payment Reversal", "Resubmit Check",
			 * "Uncollectable", "Wrong Amount", "Wrong Customer", "Wrong
			 * Invoice" // Reverse Payment debits the Customer's Account which
			 * means, nullifies earlier payments.
			 * 
			 */
			Integer offset = null;
			Integer limit = null;
			if (req.getParameter("all") != null)
			{
				offset = new Integer(0);
			} else
			{
				try
				{
					offset = new Integer(req.getParameter("offset"));
				} catch (Exception ex)
				{
					offset = new Integer(0);
				}
				try
				{
					limit = new Integer(req.getParameter("limit"));
				} catch (Exception ex)
				{
				}
				;
			}
			// Integer limit = new Integer(100);
			// Read the CDHis table ...
			// String custTxnQuery = "select * from cdhis order by txdate";
			String custTxnQuery = "select * from cdhis ";
			// custTxnQuery += " where txstatus != 'D'"; // Ignore deleted docs
			// custTxnQuery += " where custid = 'SC0001' and (txtype = 'P' or
			// txtype = 'C')"; // Don't take Loan Account
			// custTxnQuery += " where custid in (select distinct custid from
			// cdhis where currid != 1) ";
			custTxnQuery += " where custid != 'AA0001'"; // Don't take Loan
															// Account
			custTxnQuery += " and txstatus != 'H'"; // Ignore invoices on hold
			// custTxnQuery += " where txtype = 'P'"; // Don't take Loan Account
			custTxnQuery += " order by txdate ";
			if (offset != null)
				custTxnQuery += " offset " + offset.intValue();
			if (limit != null)
				custTxnQuery += " limit " + limit.intValue();
			
			Log.printVerbose(custTxnQuery);
			// String custTxnQuery = "select * from cdhis where txtype = 'I'
			// order by txdate offset 10000 limit 100";
			// String custTxnQuery = "select * from cdhis where txtype = 'I'
			// order by txdate limit 10000";
			Statement custTxnStmt = con.createStatement();
			ResultSet rsCustTxn = custTxnStmt.executeQuery(custTxnQuery);
			/*
			 * // Clear the existing tables String cleanCashAccTxn = "delete
			 * from acc_cash_transactions"; String cleanNomAccTxn = "delete from
			 * acc_nominal_account_txn"; String cleanNomAcc = "delete from
			 * acc_nominal_account"; String cleanDocLink = "delete from
			 * acc_doclink"; String cleanGenStmt = "delete from
			 * acc_generic_stmt"; String cleanReceipt = "delete from
			 * cust_receipt_index"; String cleanInvoiceItem = "delete from
			 * cust_invoice_item"; String cleanDOItem = "delete from
			 * cust_delivery_order_item"; String cleanJobsheetItem = "delete
			 * from cust_jobsheet_item"; String cleanInvoice = "delete from
			 * cust_invoice_index"; String cleanDO = "delete from
			 * cust_delivery_order_index"; String cleanJobsheet = "delete from
			 * cust_jobsheet_index"; String cleanSalesTxn = "delete from
			 * cust_sales_txn_index"; String cleanTableCounter = "delete from
			 * app_table_counter" + " where tablename ~ 'jobsheet'" + " or
			 * tablename ~ 'delivery_order'" + " or tablename ~ 'acc_doclink'" + "
			 * or tablename ~ 'invoice'"; Statement cleanStmt =
			 * jbossCon.createStatement();
			 * cleanStmt.executeUpdate(cleanCashAccTxn);
			 * cleanStmt.executeUpdate(cleanNomAccTxn);
			 * cleanStmt.executeUpdate(cleanNomAcc);
			 * cleanStmt.executeUpdate(cleanDocLink);
			 * cleanStmt.executeUpdate(cleanGenStmt);
			 * cleanStmt.executeUpdate(cleanReceipt);
			 * cleanStmt.executeUpdate(cleanInvoiceItem);
			 * cleanStmt.executeUpdate(cleanDOItem);
			 * cleanStmt.executeUpdate(cleanJobsheetItem);
			 * cleanStmt.executeUpdate(cleanInvoice);
			 * cleanStmt.executeUpdate(cleanDO);
			 * cleanStmt.executeUpdate(cleanJobsheet);
			 * cleanStmt.executeUpdate(cleanSalesTxn);
			 * cleanStmt.executeUpdate(cleanTableCounter); cleanStmt.close();
			 */
			// int count = 0;
			curTask = new Task("Extract SalesTxns", rsCustTxn.getFetchSize());
			int count = offset.intValue();
			while (rsCustTxn.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				// Log.printVerbose("Processing Txn " + count);
				Log.printDebug("Processing Txn " + count);
				count++;
				// Get the txType
				String txType = rsCustTxn.getString("txtype");
				String docNo = rsCustTxn.getString("docno");
				String docRef = rsCustTxn.getString("docRef");
				String docInfo = rsCustTxn.getString("docInfo");
				String salesRepId = rsCustTxn.getString("srid");
				String custId = rsCustTxn.getString("custid");
				String currId = rsCustTxn.getString("currid");
				BigDecimal exchRate = rsCustTxn.getBigDecimal("exch_rate");
				Timestamp txDate = rsCustTxn.getTimestamp("txdate");
				BigDecimal term = rsCustTxn.getBigDecimal("term");
				BigDecimal txAmt = rsCustTxn.getBigDecimal("txamt");
				BigDecimal lTxAmt = rsCustTxn.getBigDecimal("ltxamt");
				// BigDecimal bfAmt = rsCustTxn.getBigDecimal("bfamt");
				// BigDecimal lBfAmt = rsCustTxn.getBigDecimal("lbfamt");
				BigDecimal debitAmt = rsCustTxn.getBigDecimal("debitamt");
				BigDecimal lDebitAmt = rsCustTxn.getBigDecimal("ldebitamt");
				BigDecimal creditAmt = rsCustTxn.getBigDecimal("creditamt");
				BigDecimal lCreditAmt = rsCustTxn.getBigDecimal("lcreditamt");
				String postDate = rsCustTxn.getString("postdate");
				String comment = rsCustTxn.getString("comment");
				String txStatus = rsCustTxn.getString("txstatus");
				String batchNo = rsCustTxn.getString("batchno");
				String itemNo = rsCustTxn.getString("itemno");
				BigDecimal netSales = rsCustTxn.getBigDecimal("netsales");
				BigDecimal lNetSales = rsCustTxn.getBigDecimal("lnetsales");
				BigDecimal lTotalCost = rsCustTxn.getBigDecimal("ltotalcost");
				String ageingDate = rsCustTxn.getString("ageingdate");
				String lIntDate = rsCustTxn.getString("lintdate");
				// Check if Customer exist
				CustAccount thisCustAcc = CustAccountNut.getObjectByCode(custId);
				if (thisCustAcc == null)
				{
					Log.printDebug(count + ": Cust Code = " + custId
							+ " doesn't exist in StkMaster, creating a temp one!!");
					// continue;
					thisCustAcc = CustAccountNut.getHome().create(custId, custId, custId,
							CustAccountBean.ACCTYPE_CORPORATE, TimeFormat.getTimestamp(), usrid);
				}
				// Check if SalesRep exist, else create new
				Integer thisSalesRepId = UserNut.getUserId(salesRepId);
				if (thisSalesRepId == null)
				{
					Log.printDebug(count + ": Sales Rep = " + salesRepId + " doesn't exist!!. Creating new ... ");
					thisSalesRepId = createNewSRep(salesRepId, salesRepId);
					// continue;
				}
				// Add docNo into the remarks
				String remarks = comment + "(Old DocRef = " + docNo + ")";
				String thisCurr = (String) hmCurr.get(new Integer(currId));
				if (!thisCurr.equals("MYR"))
				{
					remarks += ", (CURR=" + thisCurr + ")";
				}
				// Derive the LAmt instead of trusting the lTxAmt
				/*
				 * BigDecimal derivedLAmt =
				 * txAmt.divide(exchRate,BigDecimal.ROUND_HALF_UP);
				 */
				BigDecimal derivedLAmt = lDebitAmt;
				if (txType.equals("I"))
				{
					// This is an Invoice
					Log.printDebug(count + ": Processing Invoice ... ");
					if (txStatus.equals("D"))
					{
						Log.printDebug("STATUS = D, Skipping this entry ... ");
						continue;
					}
					/*
					 * DEPRECATED // Create Sales Txn Log.printVerbose(count + ":
					 * Creating SalesTxn ... "); SalesTxn newSalesTxn =
					 * SalesTxnNut.getHome().create(thisCustAcc.getPkid(),
					 * iDefSvcCtrId,txDate,remarks,SalesTxnBean.ST_CREATED,
					 * "",new Integer(0),TimeFormat.getTimestamp(),usrid);
					 * if(txStatus.equals("D"))
					 * newSalesTxn.setStatus(SalesTxnBean.STATUS_CANCELLED);
					 */
					/*
					 * SUPPRESS CREATION OF SALESORDER AND DO // Automatically
					 * create a SalesOrder Log.printVerbose(count + ": Creating
					 * SalesOrder ... "); Jobsheet newSO =
					 * //JobsheetNut.getHome().create(newSalesTxn.getPkid(),
					 * JobsheetNut.getHome().create(new Long(0),
					 * thisCurr,thisSalesRepId,thisSalesRepId,remarks,
					 * JobsheetBean.TYPE_SO,JobsheetBean.STATE_DO_OK,
					 * TimeFormat.getTimestamp(),TimeFormat.getTimestamp(),
					 * usrid); if(txStatus.equals("D"))
					 * newSO.setStatus(JobsheetBean.STATUS_CANCELLED);
					 *  // Query for Invoice Details from CDHisD String
					 * txnDQuery = "select * from cdhisd where docref = '" +
					 * docNo + "'"; Statement txnDStmt = con.createStatement();
					 * ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					 *  // Create DO if row > 0 DeliveryOrder newDO = null; if
					 * (rsTxnD.getFetchSize() > 0) { newDO =
					 * DeliveryOrderNut.getHome().create( //
					 * newSalesTxn.getPkid(), // deprecated new Long(0),
					 * thisSalesRepId,thisSalesRepId,remarks,
					 * DeliveryOrderBean.STATE_CREATED,
					 * txDate,TimeFormat.getTimestamp(),usrid);
					 * if(txStatus.equals("D"))
					 * newDO.setStatus(DeliveryOrderBean.STATUS_CANCELLED);
					 *  // Advance the SO state to DO_OK
					 * newSO.setState(JobsheetBean.STATE_DO_OK); }
					 */
					// Query for Invoice Details from CDHisD
					String txnDQuery = "select * from cdhisd where docref = '" + docNo + "'";
					Statement txnDStmt = con.createStatement();
					ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					// Automatically create an Invoice
					Log.printVerbose(count + ": Creating Invoice ... ");
					// BigDecimal derivedLAmt =
					// txAmt.divide(exchRate,BigDecimal.ROUND_HALF_UP);
					if (derivedLAmt.signum() == 0)
					{
						// Use the lDebitAmt instead
						derivedLAmt = lDebitAmt;
					}
					Invoice newInvoice = null;
					InvoiceObject newInvObj = new InvoiceObject();
					/** Populate the InvoiceObject * */
					// newInvObj.mPkid = new Long("0");
					// newInvObj.mStmtNumber= new Long("0");
					// newInvObj.mSalesTxnId = // deprecated
					// newInvObj.mPaymentTermsId = InvoiceBean.PAYMENT_CASH;
					newInvObj.mTimeIssued = txDate;
					// newInvObj.mCurrency = thisCurr;
					newInvObj.mCurrency = "MYR";
					newInvObj.mTotalAmt = derivedLAmt;
					newInvObj.mOutstandingAmt = derivedLAmt;
					newInvObj.mRemarks = remarks;
					newInvObj.mState = InvoiceBean.ST_POSTED;
					// newInvObj.mStatus = InvoiceBean.STATUS_ACTIVE;
					// newInvObj.mLastUpdate = TimeFormat.getTimestamp();
					newInvObj.mUserIdUpdate = usrid;
					newInvObj.mEntityTable = CustAccountBean.TABLENAME;
					newInvObj.mEntityKey = thisCustAcc.getPkid();
					newInvObj.mEntityName = thisCustAcc.getName(); // ???
					// newInvObj.mEntityType = ""; // ???
					// newInvObj.mIdentityNumber = "";
					newInvObj.mEntityContactPerson = newInvObj.mEntityName;
					// newInvObj.mForeignTable = "";
					// newInvObj.mForeignKey = new Integer(0);
					// newInvObj.mForeignText = "";
					// In order to derive the locationId and pccenter,
					// need to get the custsvcObject
					newInvObj.mCustSvcCtrId = iDefSvcCtrId;
					CustServiceCenterObject thisCSCObj = CustServiceCenterNut.getObject(newInvObj.mCustSvcCtrId);
					newInvObj.mLocationId = thisCSCObj.invLocationId;
					newInvObj.mPCCenter = thisCSCObj.accPCCenterId;
					// newInvObj.mTxnType = "";
					// newInvObj.mStmtType = "";
					// newInvObj.mReferenceNo = "";
					// newInvObj.mDescription = "";
					// newInvObj.mWorkOrder = new Long(0);
					// newInvObj.mDeliveryOrder = new Long(0);
					// newInvObj.mReceiptId = new Long(0);
					// newInvObj.mDisplayFormat = "inv1";
					// newInvObj.mDocType = "inv";
					newInvoice = InvoiceNut.fnCreate(newInvObj);
					// / ADDED BY VINCENT LEE - 2005-06-15
					{
						NominalAccountObject naObj = NominalAccountNut.getObject(CustAccountBean.TABLENAME,
								newInvObj.mPCCenter, newInvObj.mEntityKey, newInvObj.mCurrency);
						if (naObj == null)
						{
							naObj = new NominalAccountObject();
							// code = "not_used";
							naObj.namespace = NominalAccountBean.NS_CUSTOMER;
							naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
							naObj.foreignKey = newInvObj.mEntityKey;
							naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
							naObj.currency = newInvObj.mCurrency;
							naObj.amount = new BigDecimal(0);
							naObj.remarks = newInvObj.mRemarks;
							naObj.accPCCenterId = newInvObj.mPCCenter;
							naObj.userIdUpdate = newInvObj.mUserIdUpdate;
							NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
							naObj = naEJB.getObject();
						}
						NominalAccountTxnObject natObj = new NominalAccountTxnObject();
						natObj.nominalAccount = naObj.pkid;
						natObj.foreignTable = InvoiceBean.TABLENAME;
						natObj.foreignKey = newInvObj.mPkid;
						natObj.code = "not_used";
						natObj.info1 = " ";
						// natObj.description = " ";
						natObj.description = newInvObj.mRemarks;
						natObj.txnType = " ";
						natObj.glCodeDebit = GLCodeBean.ACC_RECEIVABLE;
						natObj.glCodeCredit = GLCodeBean.GENERAL_SALES;
						natObj.currency = newInvObj.mCurrency;
						natObj.amount = newInvObj.mTotalAmt;
						natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
						natObj.timeParam1 = newInvObj.mTimeIssued;
						natObj.timeOption2 = NominalAccountTxnBean.TIME_DUE;
						natObj.timeParam2 = newInvObj.mTimeIssued;
						natObj.state = NominalAccountTxnBean.ST_ACTUAL;
						natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
						natObj.lastUpdate = TimeFormat.getTimestamp();
						natObj.userIdUpdate = newInvObj.mUserIdUpdate;
						NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
					}
					/*
					 * if(txStatus.equals("D"))
					 * newInvoice.setStatus(InvoiceBean.STATUS_CANCELLED);
					 */
					// populate the exchrate
					// newInvoice.setXRate(exchRate);
					Long newInvoiceId = newInvoice.getPkid();
					// Create Items under the Invoice
					int itemCount = 0;
					while (rsTxnD.next())
					{
						itemCount++;
						Log.printVerbose(count + ": Creating SalesOrder Item " + itemCount);
						String itemDocRef = rsTxnD.getString("docRef");
						String itemItemNo = rsTxnD.getString("itemno");
						String itemTxDate = rsTxnD.getString("txdate");
						String itemGlId = rsTxnD.getString("glid");
						String itemStkId = rsTxnD.getString("stkId");
						String itemLocId = rsTxnD.getString("locId");
						BigDecimal itemQty = rsTxnD.getBigDecimal("qty");
						String itemUom = rsTxnD.getString("uom");
						BigDecimal itemUnitPrice = rsTxnD.getBigDecimal("unitprice");
						BigDecimal itemItemTotal = rsTxnD.getBigDecimal("itemtotal");
						BigDecimal itemLItemTotal = rsTxnD.getBigDecimal("litemtotal");
						BigDecimal itemNetSales = rsTxnD.getBigDecimal("netsales");
						BigDecimal itemLNetSales = rsTxnD.getBigDecimal("lnetsales");
						BigDecimal itemLTotalCost = rsTxnD.getBigDecimal("ltotalcost");
						String itemDescOrig = rsTxnD.getString("desc1");
						String itemDesc = itemDescOrig;
						// Add itemGlId into itemDesc
						// itemDesc = itemDesc + ", (GLID=" + itemGlId + ")";
						Integer thisPOSItemId = null;
						POSItem thisPOSItem = null;
						// Find the InvItem
						if (!itemStkId.trim().equals(""))
						{
							// Use the general non-inv code for the time being
							Item thisItem = ItemNut.getObjectByCode(itemStkId);
							if (thisItem == null)
							{
								Log.printDebug(count + ": Item Code " + itemStkId
										+ " doesn't exist!! Creating a temp one ...");
								// continue;
								/**
								 * *** BEGIN: CREATE THE ITEM BASED ON CDHISD
								 * ****
								 */
								ItemObject itemObj = new ItemObject();
								// populate the properties here!!
								itemObj.code = itemStkId;
								itemObj.name = itemDesc;
								itemObj.description = itemDesc;
								itemObj.userIdUpdate = usrid;
								itemObj.enumInvType = new Integer(ItemBean.INV_TYPE_INVENTORY);
								itemObj.uom = itemUom;
								/*
								 * itemObj.priceList = sellingPrice;
								 * itemObj.priceSale = sellingPrice;
								 * itemObj.priceDisc1 = sellingPrice;
								 * itemObj.priceDisc2 = sellingPrice;
								 * itemObj.priceDisc3 = sellingPrice;
								 * itemObj.priceMin = costPrice;
								 * itemObj.fifoUnitCost = costPrice;
								 * itemObj.maUnitCost = costPrice;
								 * itemObj.waUnitCost = costPrice;
								 * itemObj.lastUnitCost = costPrice;
								 * itemObj.replacementUnitCost = costPrice;
								 * itemObj.preferredSupplier = suppAccObj.pkid;
								 */
								thisItem = ItemNut.fnCreate(itemObj);
								POSItem auxPOSItem = POSItemNut.getPOSItem(thisItem.getPkid(), POSItemBean.TYPE_INV);
								if (auxPOSItem == null)
								{
									POSItemObject posObj = new POSItemObject();
									posObj.itemFKId = thisItem.getPkid();
									posObj.itemType = POSItemBean.TYPE_INV;
									posObj.currency = "MYR";
									posObj.unitPriceStd = itemUnitPrice;
									posObj.unitPriceDiscounted = itemUnitPrice;
									posObj.unitPriceMin = itemUnitPrice;
									posObj.userIdUpdate = usrid;
									auxPOSItem = POSItemNut.fnCreate(posObj);
								}
								// Then populate the corresponding stock table
								Integer thisLocId = iDefLocId;
								if (!itemLocId.equals(""))
								{
									Location thisLoc = LocationNut.getObjectByCode(itemLocId);
									if (thisLoc != null)
										thisLocId = thisLoc.getPkid();
								}
								Stock auxStk =
								// StockNut.getObjectBy(thisItem.getPkid(),iDefLocId,
								StockNut.getObjectBy(thisItem.getPkid(), thisLocId, iDefCondId, "");
								if (auxStk == null)
								{
									StockObject newObj = new StockObject();
									newObj.itemId = thisItem.getPkid();
									newObj.locationId = thisLocId;
									newObj.accPCCenterId = iDefPCCenterId;
									newObj.userIdUpdate = usrid;
									auxStk = StockNut.fnCreate(newObj);
								}
								/** *** END: CREATE THE ITEM BASED ON CDHISD **** */
							} // end if (thisitem == null)
							// Get the corresponding POSItem
							thisPOSItem = POSItemNut.getPOSItem(thisItem.getPkid(), POSItemBean.TYPE_INV);
							/*
							 * if (thisPOSItem == null) { Log.printDebug(count + ":
							 * POSItem with itemid " + thisItem.getPkid() + "
							 * doesn't exist!!"); continue; }
							 */
							if (thisPOSItem == null)
							{
								POSItemObject posObj = new POSItemObject();
								posObj.itemFKId = thisItem.getPkid();
								posObj.itemType = POSItemBean.TYPE_INV;
								posObj.currency = "MYR";
								posObj.unitPriceStd = itemUnitPrice;
								posObj.unitPriceDiscounted = itemUnitPrice;
								posObj.unitPriceMin = itemUnitPrice;
								posObj.userIdUpdate = usrid;
								thisPOSItem = POSItemNut.fnCreate(posObj);
							}
							thisPOSItemId = thisPOSItem.getPkid();
						} // end if (stkId != "")
						else
						{
							// Assume general non-inventory
							thisPOSItemId = POSItemBean.PKID_NONINV;
						}
						/*
						 * SUPPRESS CREATION OF JS and DO Items // Create
						 * JobsheetItem JobsheetItem newJSItem =
						 * JobsheetItemNut.getHome().create(
						 * newSO.getPkid(),thisPOSItemId,
						 * itemQty,thisCurr,itemDesc,itemUnitPrice,itemUnitPrice);
						 * if(txStatus.equals("D"))
						 * newJSItem.setStatus(JobsheetItemBean.STATUS_CANCELLED);
						 *  // Create DOItem DeliveryOrderItem newDOItem =
						 * DeliveryOrderItemNut.getHome().create(
						 * newDO.getPkid(),newJSItem.getPkid(),
						 * itemQty,itemDesc); if(txStatus.equals("D"))
						 * newDOItem.setStatus(DeliveryOrderItemBean.STATUS_CANCELLED);
						 */
						// Create InvoiceItem
						InvoiceItemObject invItemObj = new InvoiceItemObject();
						invItemObj.mInvoiceId = newInvoiceId;
						invItemObj.mPosItemId = thisPOSItemId;
						invItemObj.mTotalQty = itemQty;
						// invItemObj.mCurrency = thisCurr;
						invItemObj.mCurrency = "MYR";
						invItemObj.mRemarks = itemDesc;
						invItemObj.mUnitPriceQuoted = itemUnitPrice;
						if (thisPOSItemId.intValue() != POSItemBean.PKID_NONINV.intValue())
						{
							POSItemChildObject posChild = POSItemNut.getChildObject(thisPOSItemId);
							invItemObj.mPosItemType = posChild.itemType;
							invItemObj.mItemId = posChild.itemFKId;
							invItemObj.mItemCode = posChild.code;
							invItemObj.mName = posChild.name;
						} else
						{
							// for POSItemBean.PKID_NONINV
							Item nonInvItem2 = ItemNut.getObjectByCode("non-inv");
							invItemObj.mPosItemType = POSItemBean.TYPE_NINV;
							invItemObj.mItemId = nonInvItem2.getPkid();
							invItemObj.mItemCode = "non-inv";
							invItemObj.mName = nonInvItem2.getName();
						}
						InvoiceItem newInvoiceItem = InvoiceItemNut.fnCreate(invItemObj);
						/*
						 * if(txStatus.equals("D"))
						 * newInvoiceItem.setStatus(InvoiceItemBean.STATUS_CANCELLED);
						 */
					} // end while (rsTxnD.next())
					txnDStmt.close();
					// Do some reconciliation here,
					// basically, trust the txAmt in CDHis, and not the
					// InvoiceItems
					BigDecimal totalInvItemAmt = InvoiceNut.getInvoiceAmount(newInvoiceId);
					BigDecimal invAmt = InvoiceNut.getHandle(newInvoiceId).getTotalAmt();
					BigDecimal diff = invAmt.subtract(totalInvItemAmt);
					if (diff.signum() != 0)
					{
						// Add a non-inv item to the invoice balance it up
						// Create InvoiceItem
						String auxRemarks = "Difference between TxAmt and ItemTotal";
						InvoiceItemObject invItemObj = new InvoiceItemObject();
						invItemObj.mInvoiceId = newInvoiceId;
						invItemObj.mPosItemId = POSItemBean.PKID_NONINV;
						invItemObj.mTotalQty = new BigDecimal(1.00);
						// invItemObj.mCurrency = thisCurr;
						invItemObj.mCurrency = "MYR";
						invItemObj.mRemarks = auxRemarks;
						invItemObj.mUnitPriceQuoted = diff;
						// POSItemChildObject posChild =
						// POSItemNut.getChildObject(thisPOSItem.getPkid());
						invItemObj.mPosItemType = POSItemBean.TYPE_NINV;
						// invItemObj.mItemId = posChild.itemFKId;
						// invItemObj.mItemCode = posChild.code;
						// invItemObj.mName= posChild.name;
						InvoiceItem auxInvoiceItem = InvoiceItemNut.fnCreate(invItemObj);
						/*
						 * if(txStatus.equals("D"))
						 * auxInvoiceItem.setStatus(InvoiceItemBean.STATUS_CANCELLED);
						 */
					}
					/*
					 * // Post the Invoice to Nominal Account immediately //
					 * Alex: 06/23 - Post ONLY when tx is NOT DELETED // *******
					 * BEGIN POSTING *********** if (!txStatus.equals("D")) {
					 * NominalAccountObject naObj = NominalAccountNut.getObject(
					 * NominalAccountBean.FT_CUSTOMER, //iDefPCCenterId,
					 * thisCustAcc.getPkid(), thisCurr); iDefPCCenterId,
					 * thisCustAcc.getPkid(), "MYR"); // only one nominal
					 * account (MYR) even though transactions are in foreign
					 * currencies (info captured in invoice already) // Get the
					 * Invoice Amount // BigDecimal bdInvoiceAmt =
					 * InvoiceNut.getInvoiceAmount(newInvoiceId);
					 * 
					 * if(naObj==null) { naObj = new NominalAccountObject();
					 * //naObj.pkid = new Integer("0"); //naObj.code = new
					 * String("not_used"); naObj.namespace =
					 * NominalAccountBean.NS_CUSTOMER; naObj.foreignTable =
					 * NominalAccountBean.FT_CUSTOMER; naObj.foreignKey =
					 * thisCustAcc.getPkid(); naObj.accountType =
					 * NominalAccountBean.ACC_TYPE_RECEIVABLE; //naObj.currency =
					 * thisCurr; naObj.currency = "MYR"; // always use MYR for
					 * Topcon's case naObj.amount = new BigDecimal("0.00");
					 * //naObj.amount = naObj.amount.add(invAmt);
					 * //naObj.remarks = " "; naObj.remarks = remarks;
					 * naObj.accPCCenterId = iDefPCCenterId; naObj.state =
					 * NominalAccountBean.STATE_CREATED; naObj.status =
					 * NominalAccountBean.STATUS_ACTIVE; naObj.lastUpdate =
					 * TimeFormat.getTimestamp(); naObj.userIdUpdate = usrid;
					 * NominalAccount naEJB = NominalAccountNut.fnCreate(naObj); } //
					 * Gather necessary info to create the nominal txn
					 *  // Create the nominal account txn
					 * NominalAccountTxnObject natObj = new
					 * NominalAccountTxnObject(); natObj.nominalAccount =
					 * naObj.pkid; // Primary Key natObj.foreignTable =
					 * InvoiceBean.TABLENAME; natObj.foreignKey = newInvoiceId;
					 * natObj.code = "not_used"; natObj.info1 = " ";
					 * natObj.description = remarks; natObj.txnType =" ";
					 * natObj.glCodeDebit=NominalAccountBean.GLCODE_NOMINAL;
					 * natObj.glCodeCredit= GLCodeBean.GENERAL_SALES;
					 * natObj.currency = thisCurr; //natObj.currency = "MYR"; //
					 * always use MYR for Topcon's case natObj.amount = invAmt;
					 * natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
					 * natObj.timeParam1 = txDate; natObj.timeOption2 =
					 * NominalAccountTxnBean.TIME_DUE; natObj.timeParam2 =
					 * txDate; natObj.state = NominalAccountTxnBean.ST_ACTUAL;
					 * natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
					 * natObj.lastUpdate = TimeFormat.getTimestamp();
					 * natObj.userIdUpdate = usrid;
					 * 
					 * NominalAccountTxn natEJB =
					 * NominalAccountTxnNut.fnCreate(natObj); } // end if
					 * (txStatus != Deleted)
					 *  // ******* END POSTING ***********
					 */
				} // end if(txType = I) // INVOICE
				else if (txType.equals("P") && !txStatus.equals("D")) // for
																		// Payment
				{
					Log.printDebug("*** Detected a Receipt ***");
					// Query for Details from CDHisD
					String txnDQuery = "select * from cdhisd where docref = '" + docNo + "'";
					Statement txnDStmt = con.createStatement();
					ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					String strPayMethod = "cash"; // default to cash
					String strChequeNo = "";
					Integer iCashAccId = iDefCashAccId;
					// while(rsTxnD.next())
					if (rsTxnD.next())
					{
						// String glId = rsTxnD.getString("glid");
						String desc1 = rsTxnD.getString("desc1").trim();
						if (!desc1.equals("") && !desc1.startsWith("0/"))
						{
							strPayMethod = "cheque";
							strChequeNo = desc1;
							iCashAccId = iDefChequeAccId;
						}
						// remarks = remarks + ", (GLID=" + glId
						remarks = remarks + " (DESC=" + desc1 + ")";
					}
					txnDStmt.close();
					NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER,
					// iDefPCCenterId, thisCustAcc.getPkid(), thisCurr);
							iDefPCCenterId, thisCustAcc.getPkid(), "MYR");
					if (naObj == null)
					{
						naObj = new NominalAccountObject();
						// naObj.pkid = new Integer("0");
						// naObj.code = new String("not_used");
						naObj.namespace = NominalAccountBean.NS_CUSTOMER;
						naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
						naObj.foreignKey = thisCustAcc.getPkid();
						naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
						// naObj.currency = thisCurr;
						naObj.currency = "MYR";
						// naObj.amount = naObj.amount.add(lTxAmt);
						naObj.remarks = remarks;
						naObj.accPCCenterId = iDefPCCenterId;
						naObj.state = NominalAccountBean.STATE_CREATED;
						naObj.status = NominalAccountBean.STATUS_ACTIVE;
						naObj.lastUpdate = TimeFormat.getTimestamp();
						naObj.userIdUpdate = usrid;
						NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
					}
					/*
					 * // if derivedLAmt = 0, take the sum from payrefd if
					 * (derivedLAmt.signum() == 0) { derivedLAmt =
					 * getPaymentSum(con, docNo); Log.printVerbose(docNo + ":
					 * derivedLAmt = " + derivedLAmt); }
					 */
					// Alex (07/03) - must get from payrefd ALL THE TIME,
					// since we don't know if the payment if for multiple
					// customers
					// Vector vecPayment = getPaymentSum(con, docNo, thisCurr,
					// remarks, usrid);
					Vector vecPayment = getPaymentSum(con, docNo, "MYR", remarks, usrid);
					if (vecPayment.size() == 0)
					{
						Long newRcptId = createCustReceipt(naObj.pkid, iCashAccId, txDate, txDate, txDate,
						// thisCurr,lCreditAmt,strPayMethod,remarks,
								"MYR", lCreditAmt, strPayMethod, remarks, strChequeNo, // ""
																						// for
																						// Cash
								usrid, TimeFormat.getTimestamp());
					} else
					{
						for (int pymtCnt = 0; pymtCnt < vecPayment.size(); pymtCnt++)
						{
							Log.printVerbose("*** pymtCnt = " + pymtCnt + " ***");
							Payment thisPymt = (Payment) vecPayment.get(pymtCnt);
							// Create a Receipt
							Long newRcptId = createCustReceipt(thisPymt.nomAccId, iCashAccId, txDate, txDate, txDate,
							// thisCurr,thisPymt.amt,strPayMethod,remarks,
									"MYR", thisPymt.amt, strPayMethod, remarks, strChequeNo, // ""
																								// for
																								// Cash
									usrid, TimeFormat.getTimestamp());
						} // end for
					} // end if (vecPayment.size() == 0)
					/*
					 * // Find the invoices mapped to this receipt String
					 * doc2DocQ = "select * from payrefd where payno = '" +
					 * docNo + "'"; Statement doc2DocStmt =
					 * con.createStatement(); ResultSet rsDoc2Doc =
					 * doc2DocStmt.executeQuery(doc2DocQ);
					 * 
					 * String findInvQ = "select pkid from cust_invoice_index
					 * where remarks ~ ?"; PreparedStatement findInvStmt =
					 * jbossCon.prepareStatement(findInvQ);
					 * 
					 * while (rsDoc2Doc.next()) { String payNo =
					 * rsDoc2Doc.getString("payno"); String payType =
					 * rsDoc2Doc.getString("paytype"); String payInfo =
					 * rsDoc2Doc.getString("payinfo"); BigDecimal
					 * payRefDExchRate = rsDoc2Doc.getBigDecimal("exch_rate");
					 * String payRefDTxRefNo = rsDoc2Doc.getString("txrefno");
					 * String payRefDTxInfo = rsDoc2Doc.getString("txinfo");
					 * String payRefTxType = rsDoc2Doc.getString("txType");
					 * BigDecimal payRefDTxExchRate =
					 * rsDoc2Doc.getBigDecimal("txexch_rate"); Timestamp
					 * payRefDTxDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("txdate"), "MM/dd/yy HH:mm:ss");
					 * Timestamp payDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("paydate"), "MM/dd/yy HH:mm:ss");
					 * BigDecimal payAmt = rsDoc2Doc.getBigDecimal("payamt");
					 * BigDecimal lPayAmt = rsDoc2Doc.getBigDecimal("lpayamt");
					 * BigDecimal payRefDLTxAmt =
					 * rsDoc2Doc.getBigDecimal("ltxamt"); String posted =
					 * rsDoc2Doc.getString("posted"); String mthEnd =
					 * rsDoc2Doc.getString("mthend"); String payRefDCustId =
					 * rsDoc2Doc.getString("custId");
					 *  // Find the Invoice PKID corresponding to txRefNo
					 * findInvStmt.setString(1, payRefDTxRefNo); ResultSet
					 * rsFindInv = findInvStmt.executeQuery(); Long thisInvId =
					 * null; if(rsFindInv.next()) { thisInvId = new
					 * Long(rsFindInv.getLong("pkid"));
					 *  // Create the DocLink DocLink newDocLink =
					 * DocLinkNut.getHome().create( DocLinkBean.NS_CUSTOMER, "",
					 * DocLinkBean.RELTYPE_PAYMENT, ReceiptBean.TABLENAME,
					 * newRcptId, InvoiceBean.TABLENAME, thisInvId, thisCurr,
					 * lPayAmt, "", payAmt, "", TimeFormat.getTimestamp(),
					 * usrid); } else { //throw new Exception("Could not find
					 * Invoice " + payRefDTxRefNo); Log.printDebug("Could not
					 * find Invoice " + payRefDTxRefNo); } findInvStmt.close(); }
					 * doc2DocStmt.close();
					 */
				} // end if(txType = P) // PAYMENT
				else if (txType.equals("C") && !txStatus.equals("D")) // for
																		// CreditNote
				{
					Log.printDebug("*** Detected a CreditNote ***");
					// Query for Details from CDHisD
					String txnDQuery = "select * from cdhisd where docref = '" + docNo + "'";
					Statement txnDStmt = con.createStatement();
					ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					String creditNoteRemarks = remarks;
					while (rsTxnD.next())
					{
						String glId = rsTxnD.getString("glid");
						String stkId = rsTxnD.getString("stkid");
						String desc1 = rsTxnD.getString("desc1");
						if (!stkId.equals(""))
						{
							creditNoteRemarks = creditNoteRemarks + "," + stkId;
						} else
						{
							// remarks = remarks + ", (GLID=" + glId
							remarks = remarks + " (DESC=" + desc1 + ")";
							// creditNoteRemarks = creditNoteRemarks + ",
							// (GLID=" + glId
							creditNoteRemarks = creditNoteRemarks + " (DESC=" + desc1 + ")";
						}
						// Truncate remarks & creditNoteRemarks to 500 char
						if (remarks.length() > NominalAccountBean.MAX_LEN_REMARKS)
						{
							remarks = remarks.substring(0, NominalAccountBean.MAX_LEN_REMARKS);
						}
						if (creditNoteRemarks.length() > GenericStmtBean.MAX_LEN_REMARKS)
						{
							creditNoteRemarks = creditNoteRemarks.substring(0, GenericStmtBean.MAX_LEN_REMARKS);
						}
					}
					txnDStmt.close();
					NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER,
					// iDefPCCenterId, thisCustAcc.getPkid(), thisCurr);
							iDefPCCenterId, thisCustAcc.getPkid(), "MYR");
					if (naObj == null)
					{
						naObj = new NominalAccountObject();
						// naObj.pkid = new Integer("0");
						// naObj.code = new String("not_used");
						naObj.namespace = NominalAccountBean.NS_CUSTOMER;
						naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
						naObj.foreignKey = thisCustAcc.getPkid();
						naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
						// naObj.currency = thisCurr;
						naObj.currency = "MYR";
						// naObj.amount = naObj.amount.add(lTxAmt);
						naObj.remarks = remarks;
						naObj.accPCCenterId = iDefPCCenterId;
						naObj.state = NominalAccountBean.STATE_CREATED;
						naObj.status = NominalAccountBean.STATUS_ACTIVE;
						naObj.lastUpdate = TimeFormat.getTimestamp();
						naObj.userIdUpdate = usrid;
						NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
					}
					/*
					 * // if derivedLAmt = 0, take the sum from payrefd if
					 * (derivedLAmt.signum() == 0) { derivedLAmt =
					 * getPaymentSum(con, docNo); Log.printVerbose(docNo + ":
					 * derivedLAmt = " + derivedLAmt); }
					 *  // Create a Credit Note Long newCNId = createCreditNote(
					 * naObj.pkid, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
					 * GLCodeBean.CASH_DISCOUNT, txDate, InvoiceBean.TABLENAME,
					 * new Long(0), docNo, creditNoteRemarks,
					 * TimeFormat.getTimestamp(), usrid, derivedLAmt);
					 */
					// Alex (07/03) - must get from payrefd ALL THE TIME,
					// since we don't know if the payment if for multiple
					// customers
					// Vector vecPayment = getPaymentSum(con, docNo, thisCurr,
					// remarks,usrid);
					Vector vecPayment = getPaymentSum(con, docNo, "MYR", remarks, usrid);
					if (vecPayment.size() == 0)
					{
						Long newCNId = createCreditNote(naObj.pkid, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
								GLCodeBean.CASH_DISCOUNT, txDate, InvoiceBean.TABLENAME, new Long(0), docNo,
								creditNoteRemarks, TimeFormat.getTimestamp(),
								// usrid, thisCurr, lCreditAmt);
								usrid, "MYR", lCreditAmt);
					} else
					{
						for (int pymtCnt = 0; pymtCnt < vecPayment.size(); pymtCnt++)
						{
							Log.printVerbose("*** pymtCnt = " + pymtCnt + " ***");
							Payment thisPymt = (Payment) vecPayment.get(pymtCnt);
							// Create a Credit Note
							Long newCNId = createCreditNote(
							// naObj.pkid,
							// GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
									thisPymt.nomAccId, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
									GLCodeBean.CASH_DISCOUNT, txDate, InvoiceBean.TABLENAME, new Long(0), docNo,
									creditNoteRemarks, TimeFormat.getTimestamp(),
									// usrid, thisCurr, thisPymt.amt);
									usrid, "MYR", thisPymt.amt);
						} // end for
					} // end if (vecPayment.size() == 0)
					/*
					 * // Find the invoices mapped to this receipt String
					 * doc2DocQ = "select * from payrefd where payno = '" +
					 * docNo + "'"; Statement doc2DocStmt =
					 * con.createStatement(); ResultSet rsDoc2Doc =
					 * doc2DocStmt.executeQuery(doc2DocQ);
					 * 
					 * String findInvQ = "select pkid from cust_invoice_index
					 * where remarks ~ ?"; PreparedStatement findInvStmt =
					 * jbossCon.prepareStatement(findInvQ);
					 * 
					 * while (rsDoc2Doc.next()) { String payNo =
					 * rsDoc2Doc.getString("payno"); String payType =
					 * rsDoc2Doc.getString("paytype"); String payInfo =
					 * rsDoc2Doc.getString("payinfo"); BigDecimal
					 * payRefDExchRate = rsDoc2Doc.getBigDecimal("exch_rate");
					 * String payRefDTxRefNo = rsDoc2Doc.getString("txrefno");
					 * String payRefDTxInfo = rsDoc2Doc.getString("txinfo");
					 * String payRefTxType = rsDoc2Doc.getString("txType");
					 * BigDecimal payRefDTxExchRate =
					 * rsDoc2Doc.getBigDecimal("txexch_rate"); Timestamp
					 * payRefDTxDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("txdate"), "MM/dd/yy HH:mm:ss");
					 * Timestamp payDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("paydate"), "MM/dd/yy HH:mm:ss");
					 * BigDecimal payAmt = rsDoc2Doc.getBigDecimal("payamt");
					 * BigDecimal lPayAmt = rsDoc2Doc.getBigDecimal("lpayamt");
					 * BigDecimal payRefDLTxAmt =
					 * rsDoc2Doc.getBigDecimal("ltxamt"); String posted =
					 * rsDoc2Doc.getString("posted"); String mthEnd =
					 * rsDoc2Doc.getString("mthend"); String payRefDCustId =
					 * rsDoc2Doc.getString("custId");
					 *  // Find the Invoice PKID corresponding to txRefNo
					 * findInvStmt.setString(1, payRefDTxRefNo); ResultSet
					 * rsFindInv = findInvStmt.executeQuery(); Long thisInvId =
					 * null; if(rsFindInv.next()) { thisInvId = new
					 * Long(rsFindInv.getLong("pkid"));
					 *  // Create the DocLink DocLink newDocLink =
					 * DocLinkNut.getHome().create( DocLinkBean.NS_CUSTOMER, "",
					 * DocLinkBean.RELTYPE_CN, GenericStmtBean.TABLENAME,
					 * newCNId, InvoiceBean.TABLENAME, thisInvId, thisCurr,
					 * lPayAmt, "", payAmt, "", TimeFormat.getTimestamp(),
					 * usrid); } else { //throw new Exception("Could not find
					 * Invoice " + payRefDTxRefNo); Log.printDebug("Could not
					 * find Invoice " + payRefDTxRefNo); } findInvStmt.close(); }
					 * doc2DocStmt.close();
					 */
				} // end if(txType = C) // CreditNote
				else if (txType.equals("D") && !txStatus.equals("D")) // for
																		// DebitNote
				{
					Log.printDebug("*** Detected a DebitNote ***");
					// Query for Details from CDHisD
					String txnDQuery = "select * from cdhisd where docref = '" + docNo + "'";
					Statement txnDStmt = con.createStatement();
					ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					String debitNoteRemarks = remarks;
					while (rsTxnD.next())
					{
						String glId = rsTxnD.getString("glid");
						String stkId = rsTxnD.getString("stkid");
						String desc1 = rsTxnD.getString("desc1");
						if (!stkId.equals(""))
						{
							debitNoteRemarks = debitNoteRemarks + "," + stkId;
						} else
						{
							// remarks = remarks + ", (GLID=" + glId
							remarks = remarks + " (DESC=" + desc1 + ")";
							// debitNoteRemarks = debitNoteRemarks + ", (GLID="
							// + glId
							debitNoteRemarks = debitNoteRemarks + " (DESC=" + desc1 + ")";
						}
						// Truncate debitNoteRemarks to 500 char
						if (debitNoteRemarks.length() > GenericStmtBean.MAX_LEN_REMARKS)
						{
							debitNoteRemarks = debitNoteRemarks.substring(0, GenericStmtBean.MAX_LEN_REMARKS);
						}
					}
					txnDStmt.close();
					NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER,
					// iDefPCCenterId, thisCustAcc.getPkid(), thisCurr);
							iDefPCCenterId, thisCustAcc.getPkid(), "MYR");
					if (naObj == null)
					{
						naObj = new NominalAccountObject();
						// naObj.pkid = new Integer("0");
						// naObj.code = new String("not_used");
						naObj.namespace = NominalAccountBean.NS_CUSTOMER;
						naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
						naObj.foreignKey = thisCustAcc.getPkid();
						naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
						// naObj.currency = thisCurr;
						naObj.currency = "MYR";
						// naObj.amount = naObj.amount.add(lTxAmt);
						naObj.remarks = remarks;
						naObj.accPCCenterId = iDefPCCenterId;
						naObj.state = NominalAccountBean.STATE_CREATED;
						naObj.status = NominalAccountBean.STATUS_ACTIVE;
						naObj.lastUpdate = TimeFormat.getTimestamp();
						naObj.userIdUpdate = usrid;
						NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
					}
					// if derivedLAmt = 0, take the sum from payrefd
					if (derivedLAmt.signum() == 0)
					{
						// derivedLAmt = getRevPaymentSum(con, docNo);
						derivedLAmt = lDebitAmt;
						Log.printVerbose(docNo + ": derivedLAmt = " + derivedLAmt);
					}
					// Create a Debit Note
					Long newDNId = createDebitNote(naObj.pkid, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
							GLCodeBean.INTEREST_REVENUE, txDate, InvoiceBean.TABLENAME, new Long(0), docNo,
							debitNoteRemarks, TimeFormat.getTimestamp(),
							// usrid, thisCurr, derivedLAmt);
							usrid, "MYR", derivedLAmt);
					/*
					 * // Find the invoices mapped to this receipt String
					 * doc2DocQ = "select * from payrefd where payno = '" +
					 * docNo + "'"; Statement doc2DocStmt =
					 * con.createStatement(); ResultSet rsDoc2Doc =
					 * doc2DocStmt.executeQuery(doc2DocQ);
					 * 
					 * String findInvQ = "select pkid from cust_invoice_index
					 * where remarks ~ ?"; PreparedStatement findInvStmt =
					 * jbossCon.prepareStatement(findInvQ);
					 * 
					 * while (rsDoc2Doc.next()) { String payNo =
					 * rsDoc2Doc.getString("payno"); String payType =
					 * rsDoc2Doc.getString("paytype"); String payInfo =
					 * rsDoc2Doc.getString("payinfo"); BigDecimal
					 * payRefDExchRate = rsDoc2Doc.getBigDecimal("exch_rate");
					 * String payRefDTxRefNo = rsDoc2Doc.getString("txrefno");
					 * String payRefDTxInfo = rsDoc2Doc.getString("txinfo");
					 * String payRefTxType = rsDoc2Doc.getString("txType");
					 * BigDecimal payRefDTxExchRate =
					 * rsDoc2Doc.getBigDecimal("txexch_rate"); Timestamp
					 * payRefDTxDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("txdate"), "MM/dd/yy HH:mm:ss");
					 * Timestamp payDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("paydate"), "MM/dd/yy HH:mm:ss");
					 * BigDecimal payAmt = rsDoc2Doc.getBigDecimal("payamt");
					 * BigDecimal lPayAmt = rsDoc2Doc.getBigDecimal("lpayamt");
					 * BigDecimal payRefDLTxAmt =
					 * rsDoc2Doc.getBigDecimal("ltxamt"); String posted =
					 * rsDoc2Doc.getString("posted"); String mthEnd =
					 * rsDoc2Doc.getString("mthend"); String payRefDCustId =
					 * rsDoc2Doc.getString("custId");
					 *  // Find the Invoice PKID corresponding to txRefNo
					 * findInvStmt.setString(1, payRefDTxRefNo); ResultSet
					 * rsFindInv = findInvStmt.executeQuery(); Long thisInvId =
					 * null; if(rsFindInv.next()) { thisInvId = new
					 * Long(rsFindInv.getLong("pkid"));
					 *  // Create the DocLink DocLink newDocLink =
					 * DocLinkNut.getHome().create( DocLinkBean.NS_CUSTOMER, "",
					 * DocLinkBean.RELTYPE_DN, GenericStmtBean.TABLENAME,
					 * newDNId, InvoiceBean.TABLENAME, thisInvId, thisCurr,
					 * lPayAmt, "", payAmt, "", TimeFormat.getTimestamp(),
					 * usrid); } else { //throw new Exception("Could not find
					 * Invoice " + payRefDTxRefNo); Log.printDebug("Could not
					 * find Invoice " + payRefDTxRefNo); } findInvStmt.close(); }
					 * doc2DocStmt.close();
					 */
				} // end if(txType = D) // DebitNote
				else if (txType.equals("R") && !txStatus.equals("D")) // for
																		// ReversePayment
				{
					Log.printDebug("*** Detected a ReversePayment ***");
					// Query for Details from CDHisD
					String txnDQuery = "select * from cdhisd where docref = '" + docNo + "'";
					Statement txnDStmt = con.createStatement();
					ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					while (rsTxnD.next())
					{
						String glId = rsTxnD.getString("glid");
						String desc1 = rsTxnD.getString("desc1");
						// remarks = remarks + ", (GLID=" + glId
						remarks = remarks + " (DESC=" + desc1 + ")";
					}
					txnDStmt.close();
					NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER,
					// iDefPCCenterId, thisCustAcc.getPkid(), thisCurr);
							iDefPCCenterId, thisCustAcc.getPkid(), "MYR");
					if (naObj == null)
					{
						naObj = new NominalAccountObject();
						// naObj.pkid = new Integer("0");
						// naObj.code = new String("not_used");
						naObj.namespace = NominalAccountBean.NS_CUSTOMER;
						naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
						naObj.foreignKey = thisCustAcc.getPkid();
						naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
						// naObj.currency = thisCurr;
						naObj.currency = "MYR";
						// naObj.amount = naObj.amount.add(lTxAmt);
						naObj.remarks = remarks;
						naObj.accPCCenterId = iDefPCCenterId;
						naObj.state = NominalAccountBean.STATE_CREATED;
						naObj.status = NominalAccountBean.STATUS_ACTIVE;
						naObj.lastUpdate = TimeFormat.getTimestamp();
						naObj.userIdUpdate = usrid;
						NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
					}
					/*
					 * // if derivedLAmt = 0, take the sum from payrefd if
					 * (derivedLAmt.signum() == 0) { //derivedLAmt =
					 * getRevPaymentSum(con, docNo); derivedLAmt = lDebitAmt;
					 * Log.printVerbose(docNo + ": derivedLAmt = " +
					 * derivedLAmt); }
					 */
					Vector vecRevPayment = getRevPaymentSum(con, docNo, "MYR", remarks, usrid);
					if (vecRevPayment.size() == 0)
					{
						// Create a ReversePayment
						Long newRPId = createReversePayment(GenericStmtBean.STMT_TYPE_REVERSE_PAYMENT, naObj.pkid,
								iDefCashAccId, "MYR", // thisCurr,
								derivedLAmt, "", remarks, "", txDate, "", new Long(0), usrid);
					} else
					{
						for (int pymtCnt = 0; pymtCnt < vecRevPayment.size(); pymtCnt++)
						{
							Log.printVerbose("*** pymtCnt = " + pymtCnt + " ***");
							Payment thisPymt = (Payment) vecRevPayment.get(pymtCnt);
							// Create a ReversePayment
							Long newRPId = createReversePayment(GenericStmtBean.STMT_TYPE_REVERSE_PAYMENT,
									thisPymt.nomAccId, // naObj.pkid,
									iDefCashAccId, "MYR", // thisCurr,
									thisPymt.amt, // derivedLAmt,
									"", remarks, "", txDate, "", new Long(0), usrid);
						} // end for
					} // end if (vecPayment.size() == 0)
					// Derive the receipt mapped to this reverse payment
					// Note: Only deriving for docno that is in the form
					// R/OR#####
					Log.printVerbose("*** reversePaymentRef = " + docNo + "***");
					/*
					 * if (docNo.startsWith("R/OR")) { String receiptRef =
					 * docNo.substring(2); Log.printVerbose("*** receiptRef = " +
					 * receiptRef + "***");
					 * 
					 * String findRcptQ = "select pkid from cust_receipt_index
					 * where payment_remarks ~ " + "'" + receiptRef + "'";
					 * Statement findRcptStmt = jbossCon.createStatement();
					 * ResultSet rsFindRcpt =
					 * findRcptStmt.executeQuery(findRcptQ);
					 * 
					 * if(rsFindRcpt.next()) { Long thisRcptId = new
					 * Long(rsFindRcpt.getLong("pkid"));
					 *  // Create the DocLink DocLink newDocLink =
					 * DocLinkNut.getHome().create( DocLinkBean.NS_CUSTOMER, "",
					 * DocLinkBean.RELTYPE_REV_PAYMENT,
					 * GenericStmtBean.TABLENAME, newRPId,
					 * ReceiptBean.TABLENAME, thisRcptId, thisCurr, lTxAmt, "",
					 * txAmt, "", TimeFormat.getTimestamp(), usrid); }
					 * findRcptStmt.close(); }
					 */
				} // end if(txType = R) // ReversePayment
				else
				{
					Log.printDebug("*** UNKNOWN txtype ***");
				}
			} // end while(rsCustTxn.next())
			custTxnStmt.close();
			/*******************************************************************
			 * FIXES AFTER POPULATION OF DOC INDICES
			 ******************************************************************/
			// 1. Fix all transactions that had gainloseamt != 0 owing to
			// differing exch_rates
			// fixExRate(con, jbossCon, usrid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while converting Topcon DB: " + ex.getMessage());
		}
		Log.printVerbose("***** END: MIGRATION OF TOPCON DB *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "CREATE DOC INDEX");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		// Clean up curTask;
		curTask = null;
		return new ActionRouter("test-migrate-topcondb-page");
	}

	private Integer createNewSRep(String srId, String srDesc) throws Exception
	{
		// Create the user
		Integer newUsrId = UserNut.getUserId(srId);
		User newUsr = null;
		if (newUsrId != null)
		{
			return newUsrId;
			// newUsr = UserNut.getHandle(newUsrId);
		} else
		{
			newUsr = UserNut.getHome().create(srId, srId, srDesc, "");
		}
		// Create the user details
		Collection colUsrDet = UserDetailsNut.getCollectionByUserId(newUsr.getUserId());
		if (colUsrDet == null || colUsrDet.isEmpty())
		{
			UserDetails thisUserD = UserDetailsNut.getHome().create(newUsr.getUserId(), Calendar.getInstance(), "", "",
					"", "", Calendar.getInstance());
		}
		// Assign a default role for the user
		Collection colUsrRole = ActionDo.getUserRoleHome().findUserRolesGiven("userid", newUsr.getUserId().toString());
		if (colUsrRole == null || colUsrRole.isEmpty())
		{
			ActionDo.getUserRoleHome().create(RoleBean.ROLEID_DEVELOPER, newUsr.getUserId(), Calendar.getInstance());
		}
		return newUsr.getUserId();
	}

	private Integer getUOM(String uom) throws Exception
	{
		Integer thisUOM = new Integer(ItemBean.UOM_NONE);
		if (uom.equals("PCE") || uom.equals("PCS"))
			thisUOM = new Integer(ItemBean.UOM_PCS);
		else if (uom.equals("ROLL"))
			thisUOM = new Integer(ItemBean.UOM_ROLL);
		else if (uom.equals("SET") || uom.equals("SETS") || uom.equals("SSET"))
			thisUOM = new Integer(ItemBean.UOM_SET);
		else if (uom.equals("UNIT") || uom.equals("NOS"))
			thisUOM = new Integer(ItemBean.UOM_UNIT);
		return thisUOM;
	}

	private Long createCustReceipt(Integer naPkid, Integer caPkid, Timestamp tsReceiptDate, Timestamp tsEffFromDate,
			Timestamp tsEffToDate, String currency, BigDecimal bdPaymentAmt, String strPaymentMethod,
			String strPaymentRmks,
			// String strGLCodeCredit,
			// String strGLCodeDebit,
			String strChequeNumber,
			// String strCashAccount,
			Integer iUsrId, Timestamp tsCreate) throws Exception
	{
		Long salesTxnId = new Long("0");
		String strAmountStr = CurrencyFormat.toWords(bdPaymentAmt, "", "");
		strAmountStr = strAmountStr + " ONLY";
		// / Get objects based on parameters above
		NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
		CashAccountObject caObj = CashAccountNut.getObject(caPkid);
		if (defBranchObj == null)
			defBranchObj = BranchNut.getObject(iDefSvcCtrId);
		/*
		 * /// 1) create a printable Cust Receipt Object ReceiptObject cRcptObj =
		 * new ReceiptObject(); cRcptObj.salesTxnId = salesTxnId;
		 * cRcptObj.custAccount = naObj.foreignKey; cRcptObj.intReserved1 = new
		 * Integer("0"); cRcptObj.strReserved1 = ""; //cRcptObj.currency =
		 * naObj.currency; cRcptObj.currency = currency; cRcptObj.amountStr =
		 * strAmountStr; cRcptObj.paymentAmount = bdPaymentAmt;
		 * cRcptObj.paymentMethod = strPaymentMethod; cRcptObj.paymentTime =
		 * tsReceiptDate; cRcptObj.chequeNumber = strChequeNumber;
		 * cRcptObj.bankId = caPkid; cRcptObj.paymentRemarks = strPaymentRmks;
		 * cRcptObj.glCode = GLCodeBean.GENERAL_SALES; cRcptObj.state =
		 * ReceiptBean.ST_CREATED; cRcptObj.status = ReceiptBean.STATUS_ACTIVE;
		 * cRcptObj.lastUpdate = tsCreate; cRcptObj.userIdUpdate = iUsrId;
		 * 
		 * Receipt sPayEJB = ReceiptNut.fnCreate(cRcptObj);
		 */
		// // create the receipt
		OfficialReceiptObject receiptObj = new OfficialReceiptObject();
		receiptObj.entityTable = CustAccountBean.TABLENAME;
		receiptObj.entityKey = naObj.foreignKey;
		receiptObj.entityName = CustAccountNut.getHandle(receiptObj.entityKey).getName();
		receiptObj.currency = currency;
		receiptObj.amount = bdPaymentAmt;
		receiptObj.paymentTime = tsReceiptDate;
		receiptObj.paymentMethod = strPaymentMethod;
		receiptObj.paymentRemarks = strPaymentRmks;
		receiptObj.chequeNumber = strChequeNumber;
		receiptObj.lastUpdate = TimeFormat.getTimestamp();
		receiptObj.userIdUpdate = iUsrId;
		receiptObj.cbCash = defBranchObj.cashbookCash;
		receiptObj.cbCard = defBranchObj.cashbookCard;
		receiptObj.cbCheque = defBranchObj.cashbookCheque;
		receiptObj.cbPDCheque = defBranchObj.cashbookPDCheque;
		if (strPaymentMethod.equals("cheque"))
		{
			receiptObj.amountCheque = bdPaymentAmt;
		} else
		{
			receiptObj.amountCash = bdPaymentAmt;
		}
		// receiptObj.amountCard = this.amountCard;
		// receiptObj.amountPDCheque = this.amountPDCheque;
		// if(this.amountPDCheque.signum()>0)
		// { receiptObj.chequeNumberPD = this.chequeNumberPD;}
		// receiptObj.datePDCheque = this.pdChequeDate;
		receiptObj.branch = defBranchObj.pkid;
		receiptObj.pcCenter = iDefPCCenterId;
		OfficialReceipt offRctEJB = OfficialReceiptNut.fnCreate(receiptObj);
		Log.printVerbose(receiptObj.toString());
		if (offRctEJB == null)
		{
			throw new Exception("Failed to create Receipt!!");
		}
		// / 2) create an entry in the nominal account transaction
		/*
		 * NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		 * natObj.nominalAccount = naObj.pkid; natObj.foreignTable =
		 * NominalAccountTxnBean.FT_CUST_RECEIPT; natObj.foreignKey =
		 * receiptObj.pkid; natObj.code = "not_used"; natObj.info1 = "";
		 * natObj.description = strPaymentRmks; natObj.glCodeCredit =
		 * GLCodeBean.ACC_RECEIVABLE; natObj.glCodeDebit = caObj.accountType;
		 * natObj.currency = naObj.currency; natObj.amount =
		 * bdPaymentAmt.negate(); natObj.timeOption1 =
		 * NominalAccountTxnBean.TIME_STMT; natObj.timeParam1 = tsReceiptDate;
		 * natObj.timeOption2 = NominalAccountTxnBean.TIME_NA; natObj.timeParam2 =
		 * tsReceiptDate; natObj.state = NominalAccountTxnBean.ST_CREATED;
		 * natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
		 * natObj.lastUpdate = tsCreate; natObj.userIdUpdate = iUsrId;
		 * NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
		 * 
		 * if(natEJB == null) { try { offRctEJB.remove();} catch(Exception ex) {
		 * ex.printStackTrace();} throw new Exception("Failed to create NAT"); }
		 * /// 4) update cash account transactions CashAccTxnObject catObj = new
		 * CashAccTxnObject(); catObj.txnCode = CashAccTxnBean.TXN_CODE_DEFAULT;
		 * catObj.glCodeCredit = GLCodeBean.ACC_RECEIVABLE; catObj.glCodeDebit =
		 * caObj.accountType; catObj.personInCharge = iUsrId; catObj.accFrom =
		 * new Integer("0");// another known bank account catObj.accTo =
		 * caObj.pkId; catObj.foreignTable = CashAccTxnBean.FT_CUST_RECEIPT;
		 * catObj.foreignKey = receiptObj.pkid; catObj.currency =
		 * naObj.currency; catObj.amount = bdPaymentAmt; catObj.txnTime =
		 * tsReceiptDate; catObj.remarks = strPaymentRmks; catObj.info1 = "";
		 * catObj.info2 = ""; catObj.state = CashAccTxnBean.ST_CREATED;
		 * catObj.status = CashAccTxnBean.STATUS_ACTIVE; catObj.lastUpdate =
		 * tsCreate; catObj.userIdUpdate = iUsrId; catObj.pcCenter =
		 * caObj.pcCenter; CashAccTxn catEJB = CashAccTxnNut.fnCreate(catObj);
		 * /// 5) update cash account balance // no need to set the balance !!!
		 * hurray !!!!
		 * 
		 * /// 3) update the nominal account NominalAccount naEJB =
		 * NominalAccountNut.getHandle(naObj.pkid); BigDecimal bdLatestBal =
		 * naEJB.getAmount(); bdLatestBal = bdLatestBal.add(natObj.amount);
		 * naObj.amount = bdLatestBal;
		 */
		return receiptObj.pkid;
	}

	private Long createCreditNote(Integer naPkid, String stmtType, String glCodeDebit, Timestamp stmtDate,
			String fStmtTable, Long fStmtKey, String refNo, String remarks, Timestamp timeUpdate, Integer userId,
			String currency, BigDecimal amount) throws Exception
	{
		// try
		// {
		String strErrMsg = null;
		/*
		 * Integer naPkid = new Integer(req.getParameter("naPkid")); String
		 * stmtType = req.getParameter("stmtType"); String glCodeDebit =
		 * req.getParameter("glCodeDebit"); Timestamp stmtDate =
		 * TimeFormat.createTimestamp( req.getParameter("stmtDate")); String
		 * fStmtTable = req.getParameter("fStmtTable"); Long fStmtKey = new
		 * Long(req.getParameter("fStmtKey")); String refNo =
		 * req.getParameter("refNo"); String remarks =
		 * req.getParameter("remarks"); Timestamp timeUpdate =
		 * TimeFormat.getTimestamp(); Integer userId =
		 * UserNut.getUserId(req.getParameter("userName")); BigDecimal amount =
		 * new BigDecimal(req.getParameter("amount"));
		 */
		// / checking input variables
		if (naPkid == null)
			throw new Exception(" Invalid Nominal Account PKID ");
		NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
		GenericStmtObject gsObj = new GenericStmtObject();
		gsObj.namespace = GenericStmtBean.NS_CUSTOMER;
		gsObj.dateStmt = stmtDate;
		// // assuming Credit Note here!!!!
		gsObj.glCodeDebit = glCodeDebit;
		gsObj.glCodeCredit = GLCodeBean.ACC_RECEIVABLE;
		// gsObj.currency = naObj.currency;
		gsObj.currency = currency; // don't inherit from NominalAccount
		gsObj.amount = amount;
		gsObj.dateDue = stmtDate;
		gsObj.foreignEntityTable = naObj.foreignTable;
		gsObj.foreignEntityKey = naObj.foreignKey;
		gsObj.pcCenter = naObj.accPCCenterId;
		gsObj.stmtType = stmtType;
		gsObj.foreignStmtTable = fStmtTable;
		gsObj.foreignStmtKey = fStmtKey;
		gsObj.referenceNo = refNo;
		// gsObj.remarks = remarks;
		gsObj.remarks = "AUTO-CN: " + remarks;
		gsObj.dateStart = gsObj.dateStmt;
		gsObj.dateEnd = gsObj.dateStmt;
		gsObj.dateDue = gsObj.dateStmt;
		gsObj.dateCreated = gsObj.dateStmt;
		gsObj.dateApproved = gsObj.dateStmt;
		gsObj.dateVerified = gsObj.dateStmt;
		gsObj.dateVerified = gsObj.dateStmt;
		gsObj.dateUpdate = timeUpdate;
		gsObj.userIdCreate = userId;
		gsObj.userIdPIC = userId;
		gsObj.userIdApprove = userId;
		gsObj.userIdVerified = userId;
		gsObj.userIdUpdate = userId;
		// create a nominal account txn for this generic stmt
		NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		natObj.nominalAccount = naObj.pkid;
		natObj.foreignTable = GenericStmtBean.TABLENAME;
		natObj.description = gsObj.remarks;
		natObj.glCodeDebit = gsObj.glCodeDebit;
		natObj.glCodeCredit = gsObj.glCodeCredit;
		natObj.currency = gsObj.currency;
		natObj.amount = gsObj.amount.negate();
		natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
		natObj.timeParam1 = gsObj.dateStmt;
		natObj.state = NominalAccountTxnBean.ST_ACTUAL;
		natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
		natObj.lastUpdate = TimeFormat.getTimestamp();
		natObj.userIdUpdate = gsObj.userIdCreate;
		GenericStmt gsEJB = GenericStmtNut.fnCreate(gsObj);
		if (gsEJB == null)
		{
			throw new Exception(" Unable to create Generic Stmt Bean ");
		}
		natObj.foreignKey = gsObj.pkid;
		NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
		if (natEJB == null)
		{
			try
			{
				gsEJB.remove();
			} catch (Exception ex)
			{
				ex.printStackTrace();
				throw new Exception(" Unable to create Nominal Account Txn ");
			}
		}
		naObj.amount = naObj.amount.add(natObj.amount);
		return gsObj.pkid;
	}

	private Long createDebitNote(Integer naPkid, String stmtType, String glCodeCredit, Timestamp stmtDate,
			String fStmtTable, Long fStmtKey, String refNo, String remarks, Timestamp timeUpdate, Integer userId,
			String currency, BigDecimal amount) throws Exception
	{
		String strErrMsg = null;
		// / checking input variables
		if (naPkid == null)
			throw new Exception(" Invalid Nominal Account PKID ");
		NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
		GenericStmtObject gsObj = new GenericStmtObject();
		gsObj.namespace = GenericStmtBean.NS_CUSTOMER;
		gsObj.dateStmt = stmtDate;
		// // assuming Credit Note here!!!!
		gsObj.glCodeDebit = GLCodeBean.ACC_RECEIVABLE;
		gsObj.glCodeCredit = glCodeCredit;
		// gsObj.currency = naObj.currency;
		gsObj.currency = currency;
		gsObj.amount = amount;
		gsObj.dateDue = stmtDate;
		gsObj.foreignEntityTable = naObj.foreignTable;
		gsObj.foreignEntityKey = naObj.foreignKey;
		gsObj.pcCenter = naObj.accPCCenterId;
		gsObj.stmtType = stmtType;
		gsObj.foreignStmtTable = fStmtTable;
		gsObj.foreignStmtKey = fStmtKey;
		gsObj.referenceNo = refNo;
		// gsObj.remarks = remarks;
		gsObj.remarks = "AUTO-DN: " + remarks;
		gsObj.dateStart = gsObj.dateStmt;
		gsObj.dateEnd = gsObj.dateStmt;
		gsObj.dateDue = gsObj.dateStmt;
		gsObj.dateCreated = gsObj.dateStmt;
		gsObj.dateApproved = gsObj.dateStmt;
		gsObj.dateVerified = gsObj.dateStmt;
		gsObj.dateVerified = gsObj.dateStmt;
		gsObj.dateUpdate = timeUpdate;
		gsObj.userIdCreate = userId;
		gsObj.userIdPIC = userId;
		gsObj.userIdApprove = userId;
		gsObj.userIdVerified = userId;
		gsObj.userIdUpdate = userId;
		// create a nominal account txn for this generic stmt
		NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		natObj.nominalAccount = naObj.pkid;
		natObj.foreignTable = GenericStmtBean.TABLENAME;
		natObj.description = gsObj.remarks;
		natObj.glCodeDebit = gsObj.glCodeDebit;
		natObj.glCodeCredit = gsObj.glCodeCredit;
		natObj.currency = gsObj.currency;
		// natObj.amount = gsObj.amount.negate();
		natObj.amount = gsObj.amount;
		natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
		natObj.timeParam1 = gsObj.dateStmt;
		natObj.state = NominalAccountTxnBean.ST_ACTUAL;
		natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
		natObj.lastUpdate = TimeFormat.getTimestamp();
		natObj.userIdUpdate = gsObj.userIdCreate;
		GenericStmt gsEJB = GenericStmtNut.fnCreate(gsObj);
		if (gsEJB == null)
		{
			throw new Exception(" Unable to create Generic Stmt Bean ");
		}
		natObj.foreignKey = gsObj.pkid;
		NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
		if (natEJB == null)
		{
			try
			{
				gsEJB.remove();
			} catch (Exception ex)
			{
				ex.printStackTrace();
				throw new Exception(" Unable to create Nominal Account Txn ");
			}
		}
		naObj.amount = naObj.amount.add(natObj.amount);
		return gsObj.pkid;
	}

	private Long createReversePayment(String strStmtType, Integer iNominalAccId, Integer iCashAccountId,
			String currency, BigDecimal bdPaymentAmt, String strChequeCreditCardNo, String strRemarks, String strInfo1,
			Timestamp tsDateStmt, String strForeignStmtTable, Long iSettleStmtId, Integer usrid) throws Exception
	{
		String strErrMsg = null;
		// first of all, get the for parameters
		// String strStmtType = (String) req.getParameter("stmtType");
		// String strNominalAcc = (String)req.getParameter("nominalAcc");
		// String strCashAccount = (String)req.getParameter("cashAccount");
		// String strAmount = (String)req.getParameter("amount");
		// BigDecimal bdPaymentAmt= new BigDecimal(strAmount);
		// String strAmtInWords = (String)req.getParameter("amtInWords");
		String strAmtInWords = CurrencyFormat.toWords(bdPaymentAmt, "", "");
		strAmtInWords = strAmtInWords + " ONLY";
		/*
		 * String strChequeCreditCardNo = (String)
		 * req.getParameter("chequeCreditCardNo"); String strRemarks =
		 * (String)req.getParameter("remarks"); String strInfo1 =
		 * (String)req.getParameter("info1");
		 * 
		 * String strDateStmt = (String) req.getParameter("dateStmt");
		 * 
		 * String foreignStmtTable = (String)
		 * req.getParameter("foreignStmtTable"); String strSettleStmtId =
		 * (String) req.getParameter("foreignStmtKey"); String userName =
		 * (String) req.getParameter("userName");
		 */
		// Get the cash account object
		CashAccountObject caObj = null;
		try
		{
			caObj = CashAccountNut.getObject(
			// new Integer(strCashAccount));
					iCashAccountId);
		} catch (Exception ex)
		{ /* do nothing */
		}
		if (caObj == null)
		{
			throw new Exception("Invalid Cash Account Object ");
		}
		// Get the nominal account object
		NominalAccountObject naObj = null;
		try
		{
			naObj = NominalAccountNut.getObject(
			// new Integer(strNominalAcc));
					iNominalAccId);
		} catch (Exception ex)
		{ /* do nothing */
		}
		if (naObj == null)
		{
			throw new Exception("Error fetching Nominal Account Object ");
		}
		GenericStmtObject gsObj = new GenericStmtObject();
		try
		{
			gsObj.namespace = GenericStmtBean.NS_CUSTOMER;
			gsObj.pcCenter = naObj.accPCCenterId;
			gsObj.glCodeDebit = GLCodeBean.ACC_RECEIVABLE;
			gsObj.glCodeCredit = caObj.accountType;
			// gsObj.currency = naObj.currency;
			gsObj.currency = currency;
			gsObj.amount = bdPaymentAmt;
			gsObj.cashFrom = caObj.pkId;
			gsObj.cashTo = caObj.pkId;
			gsObj.stmtType = strStmtType;
			// gsObj.foreignStmtTable = GenericStmtBean.TABLENAME;
			gsObj.foreignStmtTable = strForeignStmtTable;
			gsObj.foreignStmtKey = iSettleStmtId;
			gsObj.chequeCreditCardNo = strChequeCreditCardNo;
			gsObj.remarks = strRemarks;
			gsObj.info1 = strInfo1;
			gsObj.info2 = strAmtInWords;
			gsObj.nominalAccount = naObj.pkid;
			gsObj.foreignEntityTable = GenericEntityAccountBean.TABLENAME;
			gsObj.foreignEntityKey = naObj.foreignKey;
			gsObj.dateStmt = tsDateStmt;
			gsObj.dateStart = tsDateStmt;
			gsObj.dateEnd = tsDateStmt;
			gsObj.dateDue = tsDateStmt;
			gsObj.dateCreated = TimeFormat.getTimestamp();
			gsObj.dateApproved = TimeFormat.getTimestamp();
			gsObj.dateVerified = TimeFormat.getTimestamp();
			gsObj.dateUpdate = TimeFormat.getTimestamp();
			gsObj.userIdCreate = usrid;
			gsObj.userIdPIC = gsObj.userIdCreate;
			gsObj.userIdApprove = gsObj.userIdCreate;
			gsObj.userIdVerified = gsObj.userIdCreate;
			gsObj.userIdUpdate = gsObj.userIdCreate;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception(" Unable to create GenericStmtObject ");
		}
		// update the cash account txn
		CashAccTxnObject catObjF = new CashAccTxnObject();
		catObjF.txnCode = CashAccTxnBean.TXN_CODE_DEFAULT;
		catObjF.glCodeDebit = gsObj.glCodeDebit;
		catObjF.glCodeCredit = gsObj.glCodeCredit;
		catObjF.personInCharge = gsObj.userIdCreate;
		// catObjF.accFrom = caObjTo.pkId;
		catObjF.accTo = caObj.pkId;
		catObjF.foreignTable = CashAccTxnBean.FT_GENERIC_STMT;
		catObjF.foreignKey = gsObj.pkid;
		catObjF.currency = gsObj.currency;
		catObjF.amount = gsObj.amount.negate();
		catObjF.txnTime = gsObj.dateStmt;
		catObjF.remarks = gsObj.remarks;
		catObjF.state = CashAccTxnBean.ST_CREATED;
		catObjF.status = CashAccTxnBean.STATUS_ACTIVE;
		catObjF.lastUpdate = TimeFormat.getTimestamp();
		catObjF.userIdUpdate = gsObj.userIdCreate;
		catObjF.pcCenter = caObj.pcCenter;
		CashAccTxnNut.fnCreate(catObjF);
		// create a nominal account txn for this generic stmt
		NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		natObj.nominalAccount = naObj.pkid;
		natObj.foreignTable = GenericStmtBean.TABLENAME;
		// the natObj.foreignKey will be assigned later after gsObj
		// is created and a pkid is obtained
		natObj.description = gsObj.remarks;
		natObj.glCodeDebit = gsObj.glCodeDebit;
		natObj.glCodeCredit = gsObj.glCodeCredit;
		natObj.currency = gsObj.currency;
		natObj.amount = gsObj.amount;
		natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
		natObj.timeParam1 = gsObj.dateStmt;
		natObj.state = NominalAccountTxnBean.ST_ACTUAL;
		natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
		natObj.lastUpdate = TimeFormat.getTimestamp();
		natObj.userIdUpdate = gsObj.userIdCreate;
		GenericStmt gsEJB = GenericStmtNut.fnCreate(gsObj);
		if (gsEJB == null)
		{
			throw new Exception(" Unable to create Generic Stmt Bean ");
		}
		natObj.foreignKey = gsObj.pkid;
		NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
		if (natEJB == null)
		{
			try
			{
				gsEJB.remove();
			} catch (Exception ex)
			{
				ex.printStackTrace();
				throw new Exception(" Unable to create Nominal Account Txn ");
			}
		}
		naObj.amount = naObj.amount.add(natObj.amount);
		return gsObj.pkid;
	}

	private Vector getPaymentSum(Connection con, String payNo, String strCurr, String remarks, Integer usrid)
			throws Exception
	{
		// drop tmp table
		try
		{
			String dropTmpTable = "drop table tmp_payrefd";
			Statement dropTmpTableStmt = con.createStatement();
			dropTmpTableStmt.executeUpdate(dropTmpTable);
		} catch (Exception ex)
		{
			// ignore
		}
		String selectDistinct = "select distinct payno, txrefno, custid, lpayamt into tmp_payrefd "
				+ "from payrefd where payno = '" + payNo + "'";
		Log.printVerbose("selectDistinct = " + selectDistinct);
		String getPymtSumQ = "select custid, sum(lpayamt) from tmp_payrefd group by custid";
		Log.printVerbose("getPymtSumQ = " + getPymtSumQ);
		String strCustId = null;
		BigDecimal bdSumOfPayments = new BigDecimal(0);
		Vector vecRtn = new Vector();
		Statement selectDistinctStmt = con.createStatement();
		Statement getPymtSumStmt = con.createStatement();
		selectDistinctStmt.executeUpdate(selectDistinct);
		ResultSet rsGetPymtSum = getPymtSumStmt.executeQuery(getPymtSumQ);
		while (rsGetPymtSum.next())
		{
			Log.printVerbose("Found a hit ...");
			// try
			// {
			strCustId = rsGetPymtSum.getString(1);
			bdSumOfPayments = rsGetPymtSum.getBigDecimal(2);
			// derive the NominalAccount
			CustAccount thisCustAcc = CustAccountNut.getObjectByCode(strCustId);
			NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER, iDefPCCenterId,
					thisCustAcc.getPkid(), strCurr);
			if (naObj == null)
			{
				naObj = new NominalAccountObject();
				// naObj.pkid = new Integer("0");
				// naObj.code = new String("not_used");
				naObj.namespace = NominalAccountBean.NS_CUSTOMER;
				naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
				naObj.foreignKey = thisCustAcc.getPkid();
				naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
				naObj.currency = strCurr;
				// naObj.amount = naObj.amount.add(lTxAmt);
				naObj.remarks = remarks;
				naObj.accPCCenterId = iDefPCCenterId;
				naObj.state = NominalAccountBean.STATE_CREATED;
				naObj.status = NominalAccountBean.STATUS_ACTIVE;
				naObj.lastUpdate = TimeFormat.getTimestamp();
				naObj.userIdUpdate = usrid;
				NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
			}
			vecRtn.add(new Payment(naObj.pkid, bdSumOfPayments));
			// }
			// catch(Exception ex)
			// {
			// }
		} // end while
		selectDistinctStmt.close();
		getPymtSumStmt.close();
		return vecRtn;
	}

	private Vector getRevPaymentSum(Connection con, String docNo, String strCurr, String remarks, Integer usrid)
			throws Exception
	{
		String getPymtSumQ = "select custid, sum(lpayamt) from "
				+ "(select distinct payno, txrefno, custid, lpayamt from payrefd " + " where txrefno = '" + docNo
				+ "') as tmp_payrefd group by custid";
		Log.printVerbose("getRevPymtSumQ = " + getPymtSumQ);
		String strCustId = null;
		BigDecimal bdSumOfPayments = new BigDecimal(0);
		Vector vecRtn = new Vector();
		Statement getPymtSumStmt = con.createStatement();
		ResultSet rsGetPymtSum = getPymtSumStmt.executeQuery(getPymtSumQ);
		while (rsGetPymtSum.next())
		{
			Log.printVerbose("Found a hit ...");
			// try
			// {
			strCustId = rsGetPymtSum.getString(1);
			bdSumOfPayments = rsGetPymtSum.getBigDecimal(2);
			// derive the NominalAccount
			CustAccount thisCustAcc = CustAccountNut.getObjectByCode(strCustId);
			NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER, iDefPCCenterId,
					thisCustAcc.getPkid(), strCurr);
			if (naObj == null)
			{
				naObj = new NominalAccountObject();
				// naObj.pkid = new Integer("0");
				// naObj.code = new String("not_used");
				naObj.namespace = NominalAccountBean.NS_CUSTOMER;
				naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
				naObj.foreignKey = thisCustAcc.getPkid();
				naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
				naObj.currency = strCurr;
				// naObj.amount = naObj.amount.add(lTxAmt);
				naObj.remarks = remarks;
				naObj.accPCCenterId = iDefPCCenterId;
				naObj.state = NominalAccountBean.STATE_CREATED;
				naObj.status = NominalAccountBean.STATUS_ACTIVE;
				naObj.lastUpdate = TimeFormat.getTimestamp();
				naObj.userIdUpdate = usrid;
				NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
			}
			vecRtn.add(new Payment(naObj.pkid, bdSumOfPayments));
			// }
			// catch(Exception ex)
			// {
			// }
		} // end while
		getPymtSumStmt.close();
		return vecRtn;
	}

	/*
	 * private BigDecimal getPaymentSum(Connection con, String payNo) throws
	 * Exception { String getPymtSumQ = "select sum(lpayamt) from payrefd where
	 * payno = '" + payNo + "'";
	 * 
	 * BigDecimal bdSumOfPayments = new BigDecimal(0); Statement getPymtSumStmt =
	 * con.createStatement(); ResultSet rsGetPymtSum =
	 * getPymtSumStmt.executeQuery(getPymtSumQ); if (rsGetPymtSum.next()) { try {
	 * bdSumOfPayments = rsGetPymtSum.getBigDecimal(1); } catch(Exception ex) { }
	 * 
	 * if(bdSumOfPayments == null) bdSumOfPayments = new BigDecimal(0); }
	 * 
	 * return bdSumOfPayments; }
	 */
	/*
	 * private BigDecimal getRevPaymentSum(Connection con, String payNo) throws
	 * Exception { String getPymtSumQ = "select sum(lpayamt) from payrefd where
	 * txrefno = '" + payNo + "'";
	 * 
	 * BigDecimal bdSumOfPayments = new BigDecimal(0); Statement getPymtSumStmt =
	 * con.createStatement(); ResultSet rsGetPymtSum =
	 * getPymtSumStmt.executeQuery(getPymtSumQ); if (rsGetPymtSum.next()) { try {
	 * bdSumOfPayments = rsGetPymtSum.getBigDecimal(1); } catch(Exception ex) { }
	 * 
	 * if(bdSumOfPayments == null) bdSumOfPayments = new BigDecimal(0); }
	 * 
	 * return bdSumOfPayments; }
	 */
	private void fixExRate(Connection topconCon, Connection jbossCon, Integer usrid) throws Exception
	{
		String findInvQ = "select pkid from cust_invoice_index where remarks ~* ? ";
		PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		String findCurrQ = "select currid from cdhis where docno = ? ";
		PreparedStatement findCurrStmt = topconCon.prepareStatement(findCurrQ);
		String queryXRateGainLose = "select * from payrefd where gainloseamt != 0 ";
		Statement xRateGainLoseStmt = topconCon.createStatement();
		ResultSet rsXRateGainLose = xRateGainLoseStmt.executeQuery(queryXRateGainLose);
		int count = 0;
		while (rsXRateGainLose.next())
		{
			Log.printVerbose("*** Processing Txn " + ++count);
			String strPayNo = rsXRateGainLose.getString("payno");
			String strTxRefNo = rsXRateGainLose.getString("txrefno");
			String strCustCode = rsXRateGainLose.getString("custid");
			Timestamp payDate = TimeFormat.createTimeStamp(rsXRateGainLose.getString("paydate"), "MM/dd/yy HH:mm:ss");
			BigDecimal bdPayExchRate = rsXRateGainLose.getBigDecimal("exch_rate");
			BigDecimal bdTxExchRate = rsXRateGainLose.getBigDecimal("txexch_rate");
			BigDecimal bdGainLoseAmt = rsXRateGainLose.getBigDecimal("gainloseamt");
			// Get the Invoice Id
			Long invId = new Long(0);
			findInvStmt.setString(1, " = " + strTxRefNo + ")");
			ResultSet rsFindInv = findInvStmt.executeQuery();
			if (rsFindInv.next())
			{
				invId = new Long(rsFindInv.getLong("pkid"));
			}
			// Get the Customer Id
			Integer custId = null;
			//findCustStmt.setString(1, strCustCode);
			findCustStmt.setString(1, strCustCode);
			ResultSet rsFindCust = findCustStmt.executeQuery();
			if (rsFindCust.next())
			{
				custId = new Integer(rsFindCust.getInt("pkid"));
			}
			// Get the Currency
			String strCurr = null;
			findCurrStmt.setString(1, strPayNo);
			ResultSet rsFindCurr = findCurrStmt.executeQuery();
			if (rsFindCurr.next())
			{
				strCurr = rsFindCurr.getString("currid");
				strCurr = (String) hmCurr.get(new Integer(strCurr));
			}
			// Get the Nominal Account
			NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER, iDefPCCenterId,
					custId, "MYR");
			if (naObj == null)
			{
				naObj = new NominalAccountObject();
				// naObj.pkid = new Integer("0");
				// naObj.code = new String("not_used");
				naObj.namespace = NominalAccountBean.NS_CUSTOMER;
				naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
				naObj.foreignKey = custId;
				naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
				// naObj.currency = thisCurr;
				naObj.currency = "MYR";
				// naObj.amount = naObj.amount.add(lTxAmt);
				// naObj.remarks = remarks;
				naObj.accPCCenterId = iDefPCCenterId;
				naObj.state = NominalAccountBean.STATE_CREATED;
				naObj.status = NominalAccountBean.STATUS_ACTIVE;
				naObj.lastUpdate = TimeFormat.getTimestamp();
				naObj.userIdUpdate = usrid;
				NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
			}
			String strRemarks = "EXCH_RATE OFFSET (txexch_rate=" + bdTxExchRate.toString() + ", pay_exch_rate="
					+ bdPayExchRate.toString() + ")";
			// if bdGainLoseAmt < 0, means we need to CN to offset the balance
			// if bdGainLoseAmt > 0, means we need to DN to offset the balance
			if (bdGainLoseAmt.signum() < 0)
			{
				Log.printVerbose("*** " + count + ": Creating CN");
				Long newCNId = createCreditNote(naObj.pkid, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
						GLCodeBean.CASH_DISCOUNT, payDate, InvoiceBean.TABLENAME, invId, strPayNo, strRemarks,
						TimeFormat.getTimestamp(), usrid, strCurr, bdGainLoseAmt.abs());
			} else
			{
				// Create a Debit Note
				Log.printVerbose("*** " + count + ": Creating DN");
				Long newDNId = createDebitNote(naObj.pkid, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
						GLCodeBean.INTEREST_REVENUE, payDate, InvoiceBean.TABLENAME, invId, strPayNo, strRemarks,
						TimeFormat.getTimestamp(), usrid, strCurr, bdGainLoseAmt.abs());
			}
		} // end while
	} // end fixExRate
	// internal class
	class Payment
	{
		public BigDecimal amt;
		public Integer nomAccId;

		Payment(Integer nomAccId, BigDecimal amt)
		{
			this.amt = amt;
			this.nomAccId = nomAccId;
		}
	}
}
package com.vlee.servlet.test;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.math.BigDecimal;
import java.sql.*;
import java.io.Serializable;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;

public class DoMigrateTopConDB implements Action
{
	private String strClassName = "DoMigrateTopConDB";
	private static Task curTask = null;
	// Constants
	private static Integer iDefLocId = new Integer(1000);
	private static Integer iDefSvcCtrId = new Integer(1);
	private static Integer iDefPCCenterId = new Integer(1000);
	private static Integer iDefCondId = new Integer(StockNut.STK_COND_GOOD);
	private static Integer iDefCashAccId = new Integer(1000);
	private static Integer iDefChequeAccId = new Integer(1001);
	private BranchObject defBranchObj = null;
	private static HashMap hmCurr = new HashMap();
	static
	{
		hmCurr.put(new Integer(1), "MYR");
		hmCurr.put(new Integer(2), "USD");
		hmCurr.put(new Integer(3), "SGD");
		hmCurr.put(new Integer(4), "4");
		hmCurr.put(new Integer(5), "5");
		hmCurr.put(new Integer(6), "6");
		hmCurr.put(new Integer(7), "7");
	}

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
			/*******************************************************************
			 * // 1. Extract BranchInfo
			 ******************************************************************/
			String query = "select * from branchinfo";
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			curTask = new Task("Extract BranchInfo", rs.getFetchSize());
			while (rs.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printVerbose("Got one ... ");
				String coNo = rs.getString("cono");
				String branchName = rs.getString("branchname");
				String coname = rs.getString("coname");
				Log.printVerbose("coNo = " + coNo);
				Log.printVerbose("branchName = " + branchName);
				Log.printVerbose("coname = " + coname);
				// insert to location, cust_svc_center, supp_svc_center
				// Assumes locaddr has a default row of pkid = 0
				// NEW: Discovered that they are 2 Locid ("00","99")
				Location newLoc = LocationNut.getObjectByCode("00");
				if (newLoc == null)
				{
					newLoc = LocationNut.getHome().create(LocationBean.NS_TRADING, "", "00", branchName + " (00)",
							coname, iDefPCCenterId, new Integer(0), TimeFormat.getTimestamp(), usrid);
				}
				Location newLoc2 = LocationNut.getObjectByCode("99");
				if (newLoc2 == null)
				{
					newLoc2 = LocationNut.getHome().create(LocationBean.NS_TRADING, "", "99", branchName + " (99)",
							coname, iDefPCCenterId, new Integer(0), TimeFormat.getTimestamp(), usrid);
				}
				Branch newBranch = BranchNut.getObjectByCode(coNo);
				if (newBranch == null)
				{
					BranchObject newBranchObj = new BranchObject();
					newBranchObj.code = coNo;
					// newBranchObj.regNo = "";
					newBranchObj.name = branchName;
					newBranchObj.description = coname;
					// newBranchObj.addr1 = req.getParameter("addr1");
					// newBranchObj.addr2 = req.getParameter("addr2");
					// newBranchObj.addr3 = req.getParameter("addr3");
					// newBranchObj.zip = req.getParameter("zip");
					// newBranchObj.state = req.getParameter("state");
					// newBranchObj.countryCode =
					// req.getParameter("countryCode");
					// newBranchObj.phoneNo = req.getParameter("phoneNo");
					// newBranchObj.faxNo = req.getParameter("faxNo");
					// newBranchObj.webUrl = req.getParameter("webUrl");
					newBranchObj.accPCCenterId = iDefPCCenterId;
					newBranchObj.invLocationId = iDefLocId;
					// newBranchObj.cashbookCash = new
					// Integer(req.getParameter("cashbookCash"));
					// newBranchObj.cashbookCard = new
					// Integer(req.getParameter("cashbookCard"));
					// newBranchObj.cashbookCheque = new
					// Integer(req.getParameter("cashbookCheque"));
					// newBranchObj.cashbookPDCheque = new
					// Integer(req.getParameter("cashbookPDCheque"));
					// newBranchObj.currency = req.getParameter("currency");
					// newBranchObj.pricing = req.getParameter("pricing");
					// newBranchObj.hotlines = req.getParameter("hotlines");
					// newBranchObj.logoURL= req.getParameter("logoURL");
					newBranch = BranchNut.fnCreate(newBranchObj);
				}
			}
			stmt.close();
			/*******************************************************************
			 * // 2. Extract Customer Info
			 ******************************************************************/
			String custQuery = "select * from cus";
			Statement custStmt = con.createStatement();
			ResultSet rsCust = custStmt.executeQuery(custQuery);
			// String cleanCust = "delete from cust_account_index where pkid !=
			// 1";
			// Statement cleanStmt = jbossCon.createStatement();
			curTask = new Task("Extract Customer", rsCust.getFetchSize());
			while (rsCust.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printVerbose("Got a customer ... ");
				String custId = rsCust.getString("custid");
				String shortName = rsCust.getString("shortname");
				String name1 = rsCust.getString("name1");
				String custType = rsCust.getString("custtype");
				String contact = rsCust.getString("contact");
				String addr1 = rsCust.getString("addr1");
				String addr2 = rsCust.getString("addr2");
				String addr3 = rsCust.getString("addr3");
				String tel = rsCust.getString("tel");
				String fax = rsCust.getString("fax");
				// Create the custAccount
				CustAccount newCustAcc = CustAccountNut.getObjectByCode(custId);
				if (newCustAcc == null)
				{
					CustAccountObject custObj = new CustAccountObject();
					custObj.name = shortName;
					custObj.custAccountCode = custId;
					custObj.description = name1;
					custObj.nameFirst = contact;
					// custObj.accType = CustAccountBean.ACCTYPE_NORMAL_ENUM;
					custObj.mainAddress1 = addr1;
					custObj.mainAddress2 = addr2;
					custObj.mainAddress3 = addr3;
					custObj.telephone1 = tel;
					custObj.faxNo = fax;
					newCustAcc = CustAccountNut.fnCreate(custObj);
					if (newCustAcc == null)
						throw new Exception("Failed to create CustAccount " + custId);
				}
			}
			custStmt.close();
			/*******************************************************************
			 * // 2a. Extract SalesRep Info
			 ******************************************************************/
			String salesRepQuery = "select * from srep";
			Statement salesRepStmt = con.createStatement();
			ResultSet rsSalesRep = salesRepStmt.executeQuery(salesRepQuery);
			curTask = new Task("Extract SalesRep", rsSalesRep.getFetchSize());
			while (rsSalesRep.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				String srId = rsSalesRep.getString("srid");
				String srDesc = rsSalesRep.getString("description");
				createNewSRep(srId, srDesc);
			} // end rsSalesRep
			/*******************************************************************
			 * // 3. Extract Stock Info
			 ******************************************************************/
			// Create a DUMMY Non-Inventory Item to track all stocks that have
			// missing or unaccounted stkId
			Item nonInvItem = ItemNut.getObjectByCode("non-inv");
			if (nonInvItem == null)
			{
				ItemObject itemObj = new ItemObject();
				// populate the properties here!!
				itemObj.code = "non-inv";
				itemObj.name = "Non-Inventory";
				itemObj.description = "General Non-Inventory";
				itemObj.userIdUpdate = usrid;
				itemObj.enumInvType = new Integer(ItemBean.INV_TYPE_NONSTK);
				nonInvItem = ItemNut.fnCreate(itemObj);
			}
			// Create a corresponding NON_INV PosItem
			POSItem nonInvPOSItem = POSItemNut.getPOSItem(nonInvItem.getPkid(), POSItemBean.TYPE_NINV);
			if (nonInvPOSItem == null)
			{
				POSItemObject newNIPosItemObj = new POSItemObject();
				newNIPosItemObj.itemFKId = nonInvItem.getPkid();
				newNIPosItemObj.itemType = POSItemBean.TYPE_NINV;
				newNIPosItemObj.currency = "MYR";
				// newNIPosItemObj.unitPriceStd = new BigDecimal("0.00");
				// newNIPosItemObj.unitPriceDiscounted = new BigDecimal("0.00");
				// newNIPosItemObj.unitPriceMin = new BigDecimal("0.00");
				// newNIPosItemObj.timeEffective = TimeFormat.getTimestamp();
				newNIPosItemObj.status = POSItemBean.STATUS_ACTIVE;
				// newNIPosItemObj.lastUpdate = TimeFormat.getTimestamp();
				newNIPosItemObj.userIdUpdate = usrid;
				// newNIPosItemObj.costOfItem = new BigDecimal("0.00");
				nonInvPOSItem = POSItemNut.fnCreate(newNIPosItemObj);
			}
			String stkQuery = "select * from stkmaster";
			Statement stkStmt = con.createStatement();
			ResultSet rsStk = stkStmt.executeQuery(stkQuery);
			// String cleanCust = "delete from cust_account_index where pkid !=
			// 1";
			// Statement cleanStmt = jbossCon.createStatement();
			curTask = new Task("Extract Stock", rsStk.getFetchSize());
			while (rsStk.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				String stkId = rsStk.getString("stkid");
				String shortName = rsStk.getString("shortname");
				String desc1 = rsStk.getString("desc1");
				String desc2 = rsStk.getString("desc2");
				String desc3 = rsStk.getString("desc3");
				BigDecimal aveUCost = rsStk.getBigDecimal("aveucost");
				BigDecimal rplUCost = rsStk.getBigDecimal("rplucost");
				BigDecimal curBalQty = rsStk.getBigDecimal("curbalqty");
				String catid = rsStk.getString("catid");// / map to category1 of
														// inv_item
				String deptid = rsStk.getString("deptid");// / map to
															// category2 of
															// inv_item
				String grpid = rsStk.getString("grpid");// / map to category3 of
														// inv_item
				// Add the item index if doesn't already exist
				Item newItem = ItemNut.getObjectByCode(stkId);
				if (newItem == null)
				{
					String uomQuery = "select distinct uom from cdhisd where stkid = '" + stkId + "'";
					Statement uomStmt = con.createStatement();
					ResultSet rsUOM = uomStmt.executeQuery(uomQuery);
					Integer thisUOM = new Integer(ItemBean.UOM_NONE);
					ItemObject itemObj = new ItemObject();
					if (rsUOM.next())
					{
						String uom = rsUOM.getString("uom");
						itemObj.uom = uom;
						if (uom.equals("PCE") || uom.equals("PCS"))
							thisUOM = new Integer(ItemBean.UOM_PCS);
						else if (uom.equals("ROLL"))
							thisUOM = new Integer(ItemBean.UOM_ROLL);
						else if (uom.equals("SET") || uom.equals("SETS") || uom.equals("SSET"))
							thisUOM = new Integer(ItemBean.UOM_SET);
						else if (uom.equals("UNIT") || uom.equals("NOS"))
							thisUOM = new Integer(ItemBean.UOM_UNIT);
					}
					// populate the properties here!!
					itemObj.code = stkId;
					itemObj.name = shortName;
					itemObj.description = desc1 + desc2 + desc3;
					itemObj.userIdUpdate = usrid;
					itemObj.enumInvType = new Integer(ItemBean.INV_TYPE_INVENTORY);
					itemObj.categoryId = new Integer(1000);
					itemObj.unitOfMeasure = thisUOM;
					itemObj.category1 = catid;
					itemObj.category2 = deptid;
					itemObj.category3 = grpid;
					newItem = ItemNut.fnCreate(itemObj);
				}
				// Then populate the POSITems
				POSItem newPOSItem = POSItemNut.getPOSItem(newItem.getPkid(), POSItemBean.TYPE_INV);
				if (newPOSItem == null)
				{
					POSItemObject newPOSItemObj = new POSItemObject();
					newPOSItemObj.itemFKId = newItem.getPkid();
					newPOSItemObj.itemType = POSItemBean.TYPE_INV;
					newPOSItemObj.currency = "MYR";
					newPOSItemObj.unitPriceStd = rplUCost;
					// newPOSItemObj.unitPriceDiscounted = new
					// BigDecimal("0.00");
					// newPOSItemObj.unitPriceMin = new BigDecimal("0.00");
					// newPOSItemObj.timeEffective = TimeFormat.getTimestamp();
					newPOSItemObj.status = POSItemBean.STATUS_ACTIVE;
					// newPOSItemObj.lastUpdate = TimeFormat.getTimestamp();
					newPOSItemObj.userIdUpdate = usrid;
					// newPOSItemObj.costOfItem = new BigDecimal("0.00");
					newPOSItem = POSItemNut.fnCreate(newPOSItemObj);
				}
				// Then populate the corresponding stock table (only 1 row for
				// each item since we only have one warehouse
				Stock newStk = StockNut.getObjectBy(newItem.getPkid(), iDefLocId, iDefCondId, "");
				if (newStk == null)
				{
					StockObject newObj = new StockObject();
					newObj.itemId = newItem.getPkid();
					newObj.locationId = iDefLocId;
					newObj.accPCCenterId = iDefPCCenterId;
					newObj.userIdUpdate = usrid;
					newStk = StockNut.fnCreate(newObj);
				}
			}
			stkStmt.close();
			/*******************************************************************
			 * // 4. Extract Sales Txn
			 ******************************************************************/
			/*
			 * a. Sort the CDHis by TxDate b. For each row, switch(txType) if
			 * (I) // for invoice Create a SalesTxn from the top down to Nominal
			 * Account, in this manner: SalesOrder -> SOItems -> DO -> DOItems ->
			 * Invoice -> InvoiceItems -> Nominal Account
			 * 
			 * The soItems/invoiceItems taken from CDHisD table (foreign key =
			 * DocRef)
			 * 
			 * if (P) // for Payment Find each invoice mapped to the payment
			 * (foreign key = docRef) If > 1 mapping, i.e. payment is used to
			 * pay > 1 invoice, put that info in the remarks of the receipt for
			 * the time being, If 1 mapping, put that into SalesTxnId (TO_DO:
			 * Waiting for the acc_settlement table to be out) Update the
			 * NominalAccount accordingly (should be done inside Receipt)
			 * 
			 * if (C) // Credit Note Create new Credit Note (using Generic Stmt)
			 * 
			 * if (D) // Debit Note Create new Debit Note (using Generic Stmt) //
			 * may not be ready yet, but leave this out first
			 * 
			 * if (R) // Reverse Payment, used to bring the Invoice back to its
			 * original state, // e.g. "Acct Closed", "Credit Card Refund
			 * Reversed or", "Nsf", "Payment Reversal", "Resubmit Check",
			 * "Uncollectable", "Wrong Amount", "Wrong Customer", "Wrong
			 * Invoice" // Reverse Payment debits the Customer's Account which
			 * means, nullifies earlier payments.
			 * 
			 */
			Integer offset = null;
			Integer limit = null;
			if (req.getParameter("all") != null)
			{
				offset = new Integer(0);
			} else
			{
				try
				{
					offset = new Integer(req.getParameter("offset"));
				} catch (Exception ex)
				{
					offset = new Integer(0);
				}
				try
				{
					limit = new Integer(req.getParameter("limit"));
				} catch (Exception ex)
				{
				}
				;
			}
			// Integer limit = new Integer(100);
			// Read the CDHis table ...
			// String custTxnQuery = "select * from cdhis order by txdate";
			String custTxnQuery = "select * from cdhis ";
			// custTxnQuery += " where txstatus != 'D'"; // Ignore deleted docs
			// custTxnQuery += " where custid = 'SC0001' and (txtype = 'P' or
			// txtype = 'C')"; // Don't take Loan Account
			// custTxnQuery += " where custid in (select distinct custid from
			// cdhis where currid != 1) ";
			custTxnQuery += " where custid != 'AA0001'"; // Don't take Loan
															// Account
			custTxnQuery += " and txstatus != 'H'"; // Ignore invoices on hold
			// custTxnQuery += " where txtype = 'P'"; // Don't take Loan Account
			custTxnQuery += " order by txdate ";
			if (offset != null)
				custTxnQuery += " offset " + offset.intValue();
			if (limit != null)
				custTxnQuery += " limit " + limit.intValue();
			// String custTxnQuery = "select * from cdhis where txtype = 'I'
			// order by txdate offset 10000 limit 100";
			// String custTxnQuery = "select * from cdhis where txtype = 'I'
			// order by txdate limit 10000";
			Statement custTxnStmt = con.createStatement();
			ResultSet rsCustTxn = custTxnStmt.executeQuery(custTxnQuery);
			/*
			 * // Clear the existing tables String cleanCashAccTxn = "delete
			 * from acc_cash_transactions"; String cleanNomAccTxn = "delete from
			 * acc_nominal_account_txn"; String cleanNomAcc = "delete from
			 * acc_nominal_account"; String cleanDocLink = "delete from
			 * acc_doclink"; String cleanGenStmt = "delete from
			 * acc_generic_stmt"; String cleanReceipt = "delete from
			 * cust_receipt_index"; String cleanInvoiceItem = "delete from
			 * cust_invoice_item"; String cleanDOItem = "delete from
			 * cust_delivery_order_item"; String cleanJobsheetItem = "delete
			 * from cust_jobsheet_item"; String cleanInvoice = "delete from
			 * cust_invoice_index"; String cleanDO = "delete from
			 * cust_delivery_order_index"; String cleanJobsheet = "delete from
			 * cust_jobsheet_index"; String cleanSalesTxn = "delete from
			 * cust_sales_txn_index"; String cleanTableCounter = "delete from
			 * app_table_counter" + " where tablename ~ 'jobsheet'" + " or
			 * tablename ~ 'delivery_order'" + " or tablename ~ 'acc_doclink'" + "
			 * or tablename ~ 'invoice'"; Statement cleanStmt =
			 * jbossCon.createStatement();
			 * cleanStmt.executeUpdate(cleanCashAccTxn);
			 * cleanStmt.executeUpdate(cleanNomAccTxn);
			 * cleanStmt.executeUpdate(cleanNomAcc);
			 * cleanStmt.executeUpdate(cleanDocLink);
			 * cleanStmt.executeUpdate(cleanGenStmt);
			 * cleanStmt.executeUpdate(cleanReceipt);
			 * cleanStmt.executeUpdate(cleanInvoiceItem);
			 * cleanStmt.executeUpdate(cleanDOItem);
			 * cleanStmt.executeUpdate(cleanJobsheetItem);
			 * cleanStmt.executeUpdate(cleanInvoice);
			 * cleanStmt.executeUpdate(cleanDO);
			 * cleanStmt.executeUpdate(cleanJobsheet);
			 * cleanStmt.executeUpdate(cleanSalesTxn);
			 * cleanStmt.executeUpdate(cleanTableCounter); cleanStmt.close();
			 */
			// int count = 0;
			curTask = new Task("Extract SalesTxns", rsCustTxn.getFetchSize());
			int count = offset.intValue();
			while (rsCustTxn.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				// Log.printVerbose("Processing Txn " + count);
				Log.printDebug("Processing Txn " + count);
				count++;
				// Get the txType
				String txType = rsCustTxn.getString("txtype");
				String docNo = rsCustTxn.getString("docno");
				String docRef = rsCustTxn.getString("docRef");
				String docInfo = rsCustTxn.getString("docInfo");
				String salesRepId = rsCustTxn.getString("srid");
				String custId = rsCustTxn.getString("custid");
				String currId = rsCustTxn.getString("currid");
				BigDecimal exchRate = rsCustTxn.getBigDecimal("exch_rate");
				Timestamp txDate = rsCustTxn.getTimestamp("txdate");
				BigDecimal term = rsCustTxn.getBigDecimal("term");
				BigDecimal txAmt = rsCustTxn.getBigDecimal("txamt");
				BigDecimal lTxAmt = rsCustTxn.getBigDecimal("ltxamt");
				// BigDecimal bfAmt = rsCustTxn.getBigDecimal("bfamt");
				// BigDecimal lBfAmt = rsCustTxn.getBigDecimal("lbfamt");
				BigDecimal debitAmt = rsCustTxn.getBigDecimal("debitamt");
				BigDecimal lDebitAmt = rsCustTxn.getBigDecimal("ldebitamt");
				BigDecimal creditAmt = rsCustTxn.getBigDecimal("creditamt");
				BigDecimal lCreditAmt = rsCustTxn.getBigDecimal("lcreditamt");
				String postDate = rsCustTxn.getString("postdate");
				String comment = rsCustTxn.getString("comment");
				String txStatus = rsCustTxn.getString("txstatus");
				String batchNo = rsCustTxn.getString("batchno");
				String itemNo = rsCustTxn.getString("itemno");
				BigDecimal netSales = rsCustTxn.getBigDecimal("netsales");
				BigDecimal lNetSales = rsCustTxn.getBigDecimal("lnetsales");
				BigDecimal lTotalCost = rsCustTxn.getBigDecimal("ltotalcost");
				String ageingDate = rsCustTxn.getString("ageingdate");
				String lIntDate = rsCustTxn.getString("lintdate");
				// Check if Customer exist
				CustAccount thisCustAcc = CustAccountNut.getObjectByCode(custId);
				if (thisCustAcc == null)
				{
					Log.printDebug(count + ": Cust Code = " + custId
							+ " doesn't exist in StkMaster, creating a temp one!!");
					// continue;
					thisCustAcc = CustAccountNut.getHome().create(custId, custId, custId,
							CustAccountBean.ACCTYPE_CORPORATE, TimeFormat.getTimestamp(), usrid);
				}
				// Check if SalesRep exist, else create new
				Integer thisSalesRepId = UserNut.getUserId(salesRepId);
				if (thisSalesRepId == null)
				{
					Log.printDebug(count + ": Sales Rep = " + salesRepId + " doesn't exist!!. Creating new ... ");
					thisSalesRepId = createNewSRep(salesRepId, salesRepId);
					// continue;
				}
				// Add docNo into the remarks
				String remarks = comment + "(Old DocRef = " + docNo + ")";
				String thisCurr = (String) hmCurr.get(new Integer(currId));
				if (!thisCurr.equals("MYR"))
				{
					remarks += ", (CURR=" + thisCurr + ")";
				}
				// Derive the LAmt instead of trusting the lTxAmt
				/*
				 * BigDecimal derivedLAmt =
				 * txAmt.divide(exchRate,BigDecimal.ROUND_HALF_UP);
				 */
				BigDecimal derivedLAmt = lDebitAmt;
				if (txType.equals("I"))
				{
					// This is an Invoice
					Log.printDebug(count + ": Processing Invoice ... ");
					if (txStatus.equals("D"))
					{
						Log.printDebug("STATUS = D, Skipping this entry ... ");
						continue;
					}
					/*
					 * DEPRECATED // Create Sales Txn Log.printVerbose(count + ":
					 * Creating SalesTxn ... "); SalesTxn newSalesTxn =
					 * SalesTxnNut.getHome().create(thisCustAcc.getPkid(),
					 * iDefSvcCtrId,txDate,remarks,SalesTxnBean.ST_CREATED,
					 * "",new Integer(0),TimeFormat.getTimestamp(),usrid);
					 * if(txStatus.equals("D"))
					 * newSalesTxn.setStatus(SalesTxnBean.STATUS_CANCELLED);
					 */
					/*
					 * SUPPRESS CREATION OF SALESORDER AND DO // Automatically
					 * create a SalesOrder Log.printVerbose(count + ": Creating
					 * SalesOrder ... "); Jobsheet newSO =
					 * //JobsheetNut.getHome().create(newSalesTxn.getPkid(),
					 * JobsheetNut.getHome().create(new Long(0),
					 * thisCurr,thisSalesRepId,thisSalesRepId,remarks,
					 * JobsheetBean.TYPE_SO,JobsheetBean.STATE_DO_OK,
					 * TimeFormat.getTimestamp(),TimeFormat.getTimestamp(),
					 * usrid); if(txStatus.equals("D"))
					 * newSO.setStatus(JobsheetBean.STATUS_CANCELLED);
					 *  // Query for Invoice Details from CDHisD String
					 * txnDQuery = "select * from cdhisd where docref = '" +
					 * docNo + "'"; Statement txnDStmt = con.createStatement();
					 * ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					 *  // Create DO if row > 0 DeliveryOrder newDO = null; if
					 * (rsTxnD.getFetchSize() > 0) { newDO =
					 * DeliveryOrderNut.getHome().create( //
					 * newSalesTxn.getPkid(), // deprecated new Long(0),
					 * thisSalesRepId,thisSalesRepId,remarks,
					 * DeliveryOrderBean.STATE_CREATED,
					 * txDate,TimeFormat.getTimestamp(),usrid);
					 * if(txStatus.equals("D"))
					 * newDO.setStatus(DeliveryOrderBean.STATUS_CANCELLED);
					 *  // Advance the SO state to DO_OK
					 * newSO.setState(JobsheetBean.STATE_DO_OK); }
					 */
					// Query for Invoice Details from CDHisD
					String txnDQuery = "select * from cdhisd where docref = '" + docNo + "'";
					Statement txnDStmt = con.createStatement();
					ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					// Automatically create an Invoice
					Log.printVerbose(count + ": Creating Invoice ... ");
					// BigDecimal derivedLAmt =
					// txAmt.divide(exchRate,BigDecimal.ROUND_HALF_UP);
					if (derivedLAmt.signum() == 0)
					{
						// Use the lDebitAmt instead
						derivedLAmt = lDebitAmt;
					}
					Invoice newInvoice = null;
					InvoiceObject newInvObj = new InvoiceObject();
					/** Populate the InvoiceObject * */
					// newInvObj.mPkid = new Long("0");
					// newInvObj.mStmtNumber= new Long("0");
					// newInvObj.mSalesTxnId = // deprecated
					// newInvObj.mPaymentTermsId = InvoiceBean.PAYMENT_CASH;
					newInvObj.mTimeIssued = txDate;
					// newInvObj.mCurrency = thisCurr;
					newInvObj.mCurrency = "MYR";
					newInvObj.mTotalAmt = derivedLAmt;
					newInvObj.mOutstandingAmt = derivedLAmt;
					newInvObj.mRemarks = remarks;
					newInvObj.mState = InvoiceBean.ST_POSTED;
					// newInvObj.mStatus = InvoiceBean.STATUS_ACTIVE;
					// newInvObj.mLastUpdate = TimeFormat.getTimestamp();
					newInvObj.mUserIdUpdate = usrid;
					newInvObj.mEntityTable = CustAccountBean.TABLENAME;
					newInvObj.mEntityKey = thisCustAcc.getPkid();
					newInvObj.mEntityName = thisCustAcc.getName(); // ???
					// newInvObj.mEntityType = ""; // ???
					// newInvObj.mIdentityNumber = "";
					newInvObj.mEntityContactPerson = newInvObj.mEntityName;
					// newInvObj.mForeignTable = "";
					// newInvObj.mForeignKey = new Integer(0);
					// newInvObj.mForeignText = "";
					// In order to derive the locationId and pccenter,
					// need to get the custsvcObject
					newInvObj.mCustSvcCtrId = iDefSvcCtrId;
					CustServiceCenterObject thisCSCObj = CustServiceCenterNut.getObject(newInvObj.mCustSvcCtrId);
					newInvObj.mLocationId = thisCSCObj.invLocationId;
					newInvObj.mPCCenter = thisCSCObj.accPCCenterId;
					// newInvObj.mTxnType = "";
					// newInvObj.mStmtType = "";
					// newInvObj.mReferenceNo = "";
					// newInvObj.mDescription = "";
					// newInvObj.mWorkOrder = new Long(0);
					// newInvObj.mDeliveryOrder = new Long(0);
					// newInvObj.mReceiptId = new Long(0);
					// newInvObj.mDisplayFormat = "inv1";
					// newInvObj.mDocType = "inv";
					newInvoice = InvoiceNut.fnCreate(newInvObj);
					// / ADDED BY VINCENT LEE - 2005-06-15
					{
						NominalAccountObject naObj = NominalAccountNut.getObject(CustAccountBean.TABLENAME,
								newInvObj.mPCCenter, newInvObj.mEntityKey, newInvObj.mCurrency);
						if (naObj == null)
						{
							naObj = new NominalAccountObject();
							// code = "not_used";
							naObj.namespace = NominalAccountBean.NS_CUSTOMER;
							naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
							naObj.foreignKey = newInvObj.mEntityKey;
							naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
							naObj.currency = newInvObj.mCurrency;
							naObj.amount = new BigDecimal(0);
							naObj.remarks = newInvObj.mRemarks;
							naObj.accPCCenterId = newInvObj.mPCCenter;
							naObj.userIdUpdate = newInvObj.mUserIdUpdate;
							NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
							naObj = naEJB.getObject();
						}
						NominalAccountTxnObject natObj = new NominalAccountTxnObject();
						natObj.nominalAccount = naObj.pkid;
						natObj.foreignTable = InvoiceBean.TABLENAME;
						natObj.foreignKey = newInvObj.mPkid;
						natObj.code = "not_used";
						natObj.info1 = " ";
						// natObj.description = " ";
						natObj.description = newInvObj.mRemarks;
						natObj.txnType = " ";
						natObj.glCodeDebit = GLCodeBean.ACC_RECEIVABLE;
						natObj.glCodeCredit = GLCodeBean.GENERAL_SALES;
						natObj.currency = newInvObj.mCurrency;
						natObj.amount = newInvObj.mTotalAmt;
						natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
						natObj.timeParam1 = newInvObj.mTimeIssued;
						natObj.timeOption2 = NominalAccountTxnBean.TIME_DUE;
						natObj.timeParam2 = newInvObj.mTimeIssued;
						natObj.state = NominalAccountTxnBean.ST_ACTUAL;
						natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
						natObj.lastUpdate = TimeFormat.getTimestamp();
						natObj.userIdUpdate = newInvObj.mUserIdUpdate;
						NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
					}
					/*
					 * if(txStatus.equals("D"))
					 * newInvoice.setStatus(InvoiceBean.STATUS_CANCELLED);
					 */
					// TODO - populate the exchrate
					// newInvoice.setXRate(exchRate);
					Long newInvoiceId = newInvoice.getPkid();
					// Create Items under the Invoice
					int itemCount = 0;
					while (rsTxnD.next())
					{
						itemCount++;
						Log.printVerbose(count + ": Creating SalesOrder Item " + itemCount);
						String itemDocRef = rsTxnD.getString("docRef");
						String itemItemNo = rsTxnD.getString("itemno");
						String itemTxDate = rsTxnD.getString("txdate");
						String itemGlId = rsTxnD.getString("glid");
						String itemStkId = rsTxnD.getString("stkId");
						String itemLocId = rsTxnD.getString("locId");
						BigDecimal itemQty = rsTxnD.getBigDecimal("qty");
						String itemUom = rsTxnD.getString("uom");
						BigDecimal itemUnitPrice = rsTxnD.getBigDecimal("unitprice");
						BigDecimal itemItemTotal = rsTxnD.getBigDecimal("itemtotal");
						BigDecimal itemLItemTotal = rsTxnD.getBigDecimal("litemtotal");
						BigDecimal itemNetSales = rsTxnD.getBigDecimal("netsales");
						BigDecimal itemLNetSales = rsTxnD.getBigDecimal("lnetsales");
						BigDecimal itemLTotalCost = rsTxnD.getBigDecimal("ltotalcost");
						String itemDescOrig = rsTxnD.getString("desc1");
						String itemDesc = itemDescOrig;
						// Add itemGlId into itemDesc
						// itemDesc = itemDesc + ", (GLID=" + itemGlId + ")";
						Integer thisPOSItemId = null;
						POSItem thisPOSItem = null;
						// Find the InvItem
						if (!itemStkId.trim().equals(""))
						{
							// Use the general non-inv code for the time being
							Item thisItem = ItemNut.getObjectByCode(itemStkId);
							if (thisItem == null)
							{
								Log.printDebug(count + ": Item Code " + itemStkId
										+ " doesn't exist!! Creating a temp one ...");
								// continue;
								/**
								 * *** BEGIN: CREATE THE ITEM BASED ON CDHISD
								 * ****
								 */
								ItemObject itemObj = new ItemObject();
								// populate the properties here!!
								itemObj.code = itemStkId;
								itemObj.name = itemDesc;
								itemObj.description = itemDesc;
								itemObj.userIdUpdate = usrid;
								itemObj.enumInvType = new Integer(ItemBean.INV_TYPE_INVENTORY);
								itemObj.uom = itemUom;
								/*
								 * itemObj.priceList = sellingPrice;
								 * itemObj.priceSale = sellingPrice;
								 * itemObj.priceDisc1 = sellingPrice;
								 * itemObj.priceDisc2 = sellingPrice;
								 * itemObj.priceDisc3 = sellingPrice;
								 * itemObj.priceMin = costPrice;
								 * itemObj.fifoUnitCost = costPrice;
								 * itemObj.maUnitCost = costPrice;
								 * itemObj.waUnitCost = costPrice;
								 * itemObj.lastUnitCost = costPrice;
								 * itemObj.replacementUnitCost = costPrice;
								 * itemObj.preferredSupplier = suppAccObj.pkid;
								 */
								thisItem = ItemNut.fnCreate(itemObj);
								POSItem auxPOSItem = POSItemNut.getPOSItem(thisItem.getPkid(), POSItemBean.TYPE_INV);
								if (auxPOSItem == null)
								{
									POSItemObject posObj = new POSItemObject();
									posObj.itemFKId = thisItem.getPkid();
									posObj.itemType = POSItemBean.TYPE_INV;
									posObj.currency = "MYR";
									posObj.unitPriceStd = itemUnitPrice;
									posObj.unitPriceDiscounted = itemUnitPrice;
									posObj.unitPriceMin = itemUnitPrice;
									posObj.userIdUpdate = usrid;
									auxPOSItem = POSItemNut.fnCreate(posObj);
								}
								// Then populate the corresponding stock table
								Integer thisLocId = iDefLocId;
								if (!itemLocId.equals(""))
								{
									Location thisLoc = LocationNut.getObjectByCode(itemLocId);
									if (thisLoc != null)
										thisLocId = thisLoc.getPkid();
								}
								Stock auxStk =
								// StockNut.getObjectBy(thisItem.getPkid(),iDefLocId,
								StockNut.getObjectBy(thisItem.getPkid(), thisLocId, iDefCondId, "");
								if (auxStk == null)
								{
									StockObject newObj = new StockObject();
									newObj.itemId = thisItem.getPkid();
									newObj.locationId = thisLocId;
									newObj.accPCCenterId = iDefPCCenterId;
									newObj.userIdUpdate = usrid;
									auxStk = StockNut.fnCreate(newObj);
								}
								/** *** END: CREATE THE ITEM BASED ON CDHISD **** */
							} // end if (thisitem == null)
							// Get the corresponding POSItem
							thisPOSItem = POSItemNut.getPOSItem(thisItem.getPkid(), POSItemBean.TYPE_INV);
							/*
							 * if (thisPOSItem == null) { Log.printDebug(count + ":
							 * POSItem with itemid " + thisItem.getPkid() + "
							 * doesn't exist!!"); continue; }
							 */
							if (thisPOSItem == null)
							{
								POSItemObject posObj = new POSItemObject();
								posObj.itemFKId = thisItem.getPkid();
								posObj.itemType = POSItemBean.TYPE_INV;
								posObj.currency = "MYR";
								posObj.unitPriceStd = itemUnitPrice;
								posObj.unitPriceDiscounted = itemUnitPrice;
								posObj.unitPriceMin = itemUnitPrice;
								posObj.userIdUpdate = usrid;
								thisPOSItem = POSItemNut.fnCreate(posObj);
							}
							thisPOSItemId = thisPOSItem.getPkid();
						} // end if (stkId != "")
						else
						{
							// Assume general non-inventory
							thisPOSItemId = POSItemBean.PKID_NONINV;
						}
						/*
						 * SUPPRESS CREATION OF JS and DO Items // Create
						 * JobsheetItem JobsheetItem newJSItem =
						 * JobsheetItemNut.getHome().create(
						 * newSO.getPkid(),thisPOSItemId,
						 * itemQty,thisCurr,itemDesc,itemUnitPrice,itemUnitPrice);
						 * if(txStatus.equals("D"))
						 * newJSItem.setStatus(JobsheetItemBean.STATUS_CANCELLED);
						 *  // Create DOItem DeliveryOrderItem newDOItem =
						 * DeliveryOrderItemNut.getHome().create(
						 * newDO.getPkid(),newJSItem.getPkid(),
						 * itemQty,itemDesc); if(txStatus.equals("D"))
						 * newDOItem.setStatus(DeliveryOrderItemBean.STATUS_CANCELLED);
						 */
						// Create InvoiceItem
						InvoiceItemObject invItemObj = new InvoiceItemObject();
						invItemObj.mInvoiceId = newInvoiceId;
						invItemObj.mPosItemId = thisPOSItemId;
						invItemObj.mTotalQty = itemQty;
						// invItemObj.mCurrency = thisCurr;
						invItemObj.mCurrency = "MYR";
						invItemObj.mRemarks = itemDesc;
						invItemObj.mUnitPriceQuoted = itemUnitPrice;
						if (thisPOSItemId.intValue() != POSItemBean.PKID_NONINV.intValue())
						{
							POSItemChildObject posChild = POSItemNut.getChildObject(thisPOSItemId);
							invItemObj.mPosItemType = posChild.itemType;
							invItemObj.mItemId = posChild.itemFKId;
							invItemObj.mItemCode = posChild.code;
							invItemObj.mName = posChild.name;
						} else
						{
							// for POSItemBean.PKID_NONINV
							Item nonInvItem2 = ItemNut.getObjectByCode("non-inv");
							invItemObj.mPosItemType = POSItemBean.TYPE_NINV;
							invItemObj.mItemId = nonInvItem2.getPkid();
							invItemObj.mItemCode = "non-inv";
							invItemObj.mName = nonInvItem2.getName();
						}
						InvoiceItem newInvoiceItem = InvoiceItemNut.fnCreate(invItemObj);
						/*
						 * if(txStatus.equals("D"))
						 * newInvoiceItem.setStatus(InvoiceItemBean.STATUS_CANCELLED);
						 */
					} // end while (rsTxnD.next())
					txnDStmt.close();
					// Do some reconciliation here,
					// basically, trust the txAmt in CDHis, and not the
					// InvoiceItems
					BigDecimal totalInvItemAmt = InvoiceNut.getInvoiceAmount(newInvoiceId);
					BigDecimal invAmt = InvoiceNut.getHandle(newInvoiceId).getTotalAmt();
					BigDecimal diff = invAmt.subtract(totalInvItemAmt);
					if (diff.signum() != 0)
					{
						// Add a non-inv item to the invoice balance it up
						// Create InvoiceItem
						String auxRemarks = "Difference between TxAmt and ItemTotal";
						InvoiceItemObject invItemObj = new InvoiceItemObject();
						invItemObj.mInvoiceId = newInvoiceId;
						invItemObj.mPosItemId = POSItemBean.PKID_NONINV;
						invItemObj.mTotalQty = new BigDecimal(1.00);
						// invItemObj.mCurrency = thisCurr;
						invItemObj.mCurrency = "MYR";
						invItemObj.mRemarks = auxRemarks;
						invItemObj.mUnitPriceQuoted = diff;
						// POSItemChildObject posChild =
						// POSItemNut.getChildObject(thisPOSItem.getPkid());
						invItemObj.mPosItemType = POSItemBean.TYPE_NINV;
						// invItemObj.mItemId = posChild.itemFKId;
						// invItemObj.mItemCode = posChild.code;
						// invItemObj.mName= posChild.name;
						InvoiceItem auxInvoiceItem = InvoiceItemNut.fnCreate(invItemObj);
						/*
						 * if(txStatus.equals("D"))
						 * auxInvoiceItem.setStatus(InvoiceItemBean.STATUS_CANCELLED);
						 */
					}
					/*
					 * // Post the Invoice to Nominal Account immediately //
					 * Alex: 06/23 - Post ONLY when tx is NOT DELETED // *******
					 * BEGIN POSTING *********** if (!txStatus.equals("D")) {
					 * NominalAccountObject naObj = NominalAccountNut.getObject(
					 * NominalAccountBean.FT_CUSTOMER, //iDefPCCenterId,
					 * thisCustAcc.getPkid(), thisCurr); iDefPCCenterId,
					 * thisCustAcc.getPkid(), "MYR"); // only one nominal
					 * account (MYR) even though transactions are in foreign
					 * currencies (info captured in invoice already) // Get the
					 * Invoice Amount // BigDecimal bdInvoiceAmt =
					 * InvoiceNut.getInvoiceAmount(newInvoiceId);
					 * 
					 * if(naObj==null) { naObj = new NominalAccountObject();
					 * //naObj.pkid = new Integer("0"); //naObj.code = new
					 * String("not_used"); naObj.namespace =
					 * NominalAccountBean.NS_CUSTOMER; naObj.foreignTable =
					 * NominalAccountBean.FT_CUSTOMER; naObj.foreignKey =
					 * thisCustAcc.getPkid(); naObj.accountType =
					 * NominalAccountBean.ACC_TYPE_RECEIVABLE; //naObj.currency =
					 * thisCurr; naObj.currency = "MYR"; // always use MYR for
					 * Topcon's case naObj.amount = new BigDecimal("0.00");
					 * //naObj.amount = naObj.amount.add(invAmt);
					 * //naObj.remarks = " "; naObj.remarks = remarks;
					 * naObj.accPCCenterId = iDefPCCenterId; naObj.state =
					 * NominalAccountBean.STATE_CREATED; naObj.status =
					 * NominalAccountBean.STATUS_ACTIVE; naObj.lastUpdate =
					 * TimeFormat.getTimestamp(); naObj.userIdUpdate = usrid;
					 * NominalAccount naEJB = NominalAccountNut.fnCreate(naObj); } //
					 * Gather necessary info to create the nominal txn
					 *  // Create the nominal account txn
					 * NominalAccountTxnObject natObj = new
					 * NominalAccountTxnObject(); natObj.nominalAccount =
					 * naObj.pkid; // Primary Key natObj.foreignTable =
					 * InvoiceBean.TABLENAME; natObj.foreignKey = newInvoiceId;
					 * natObj.code = "not_used"; natObj.info1 = " ";
					 * natObj.description = remarks; natObj.txnType =" ";
					 * natObj.glCodeDebit=NominalAccountBean.GLCODE_NOMINAL;
					 * natObj.glCodeCredit= GLCodeBean.GENERAL_SALES;
					 * natObj.currency = thisCurr; //natObj.currency = "MYR"; //
					 * always use MYR for Topcon's case natObj.amount = invAmt;
					 * natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
					 * natObj.timeParam1 = txDate; natObj.timeOption2 =
					 * NominalAccountTxnBean.TIME_DUE; natObj.timeParam2 =
					 * txDate; natObj.state = NominalAccountTxnBean.ST_ACTUAL;
					 * natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
					 * natObj.lastUpdate = TimeFormat.getTimestamp();
					 * natObj.userIdUpdate = usrid;
					 * 
					 * NominalAccountTxn natEJB =
					 * NominalAccountTxnNut.fnCreate(natObj); } // end if
					 * (txStatus != Deleted)
					 *  // ******* END POSTING ***********
					 */
				} // end if(txType = I) // INVOICE
				else if (txType.equals("P") && !txStatus.equals("D")) // for
																		// Payment
				{
					Log.printDebug("*** Detected a Receipt ***");
					// Query for Details from CDHisD
					String txnDQuery = "select * from cdhisd where docref = '" + docNo + "'";
					Statement txnDStmt = con.createStatement();
					ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					String strPayMethod = "cash"; // default to cash
					String strChequeNo = "";
					Integer iCashAccId = iDefCashAccId;
					// while(rsTxnD.next())
					if (rsTxnD.next())
					{
						// String glId = rsTxnD.getString("glid");
						String desc1 = rsTxnD.getString("desc1").trim();
						if (!desc1.equals("") && !desc1.startsWith("0/"))
						{
							strPayMethod = "cheque";
							strChequeNo = desc1;
							iCashAccId = iDefChequeAccId;
						}
						// remarks = remarks + ", (GLID=" + glId
						remarks = remarks + " (DESC=" + desc1 + ")";
					}
					txnDStmt.close();
					NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER,
					// iDefPCCenterId, thisCustAcc.getPkid(), thisCurr);
							iDefPCCenterId, thisCustAcc.getPkid(), "MYR");
					if (naObj == null)
					{
						naObj = new NominalAccountObject();
						// naObj.pkid = new Integer("0");
						// naObj.code = new String("not_used");
						naObj.namespace = NominalAccountBean.NS_CUSTOMER;
						naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
						naObj.foreignKey = thisCustAcc.getPkid();
						naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
						// naObj.currency = thisCurr;
						naObj.currency = "MYR";
						// naObj.amount = naObj.amount.add(lTxAmt);
						naObj.remarks = remarks;
						naObj.accPCCenterId = iDefPCCenterId;
						naObj.state = NominalAccountBean.STATE_CREATED;
						naObj.status = NominalAccountBean.STATUS_ACTIVE;
						naObj.lastUpdate = TimeFormat.getTimestamp();
						naObj.userIdUpdate = usrid;
						NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
					}
					/*
					 * // if derivedLAmt = 0, take the sum from payrefd if
					 * (derivedLAmt.signum() == 0) { derivedLAmt =
					 * getPaymentSum(con, docNo); Log.printVerbose(docNo + ":
					 * derivedLAmt = " + derivedLAmt); }
					 */
					// Alex (07/03) - must get from payrefd ALL THE TIME,
					// since we don't know if the payment if for multiple
					// customers
					// Vector vecPayment = getPaymentSum(con, docNo, thisCurr,
					// remarks, usrid);
					Vector vecPayment = getPaymentSum(con, docNo, "MYR", remarks, usrid);
					if (vecPayment.size() == 0)
					{
						Long newRcptId = createCustReceipt(naObj.pkid, iCashAccId, txDate, txDate, txDate,
						// thisCurr,lCreditAmt,strPayMethod,remarks,
								"MYR", lCreditAmt, strPayMethod, remarks, strChequeNo, // ""
																						// for
																						// Cash
								usrid, TimeFormat.getTimestamp());
					} else
					{
						for (int pymtCnt = 0; pymtCnt < vecPayment.size(); pymtCnt++)
						{
							Log.printVerbose("*** pymtCnt = " + pymtCnt + " ***");
							Payment thisPymt = (Payment) vecPayment.get(pymtCnt);
							// Create a Receipt
							Long newRcptId = createCustReceipt(thisPymt.nomAccId, iCashAccId, txDate, txDate, txDate,
							// thisCurr,thisPymt.amt,strPayMethod,remarks,
									"MYR", thisPymt.amt, strPayMethod, remarks, strChequeNo, // ""
																								// for
																								// Cash
									usrid, TimeFormat.getTimestamp());
						} // end for
					} // end if (vecPayment.size() == 0)
					/*
					 * // Find the invoices mapped to this receipt String
					 * doc2DocQ = "select * from payrefd where payno = '" +
					 * docNo + "'"; Statement doc2DocStmt =
					 * con.createStatement(); ResultSet rsDoc2Doc =
					 * doc2DocStmt.executeQuery(doc2DocQ);
					 * 
					 * String findInvQ = "select pkid from cust_invoice_index
					 * where remarks ~ ?"; PreparedStatement findInvStmt =
					 * jbossCon.prepareStatement(findInvQ);
					 * 
					 * while (rsDoc2Doc.next()) { String payNo =
					 * rsDoc2Doc.getString("payno"); String payType =
					 * rsDoc2Doc.getString("paytype"); String payInfo =
					 * rsDoc2Doc.getString("payinfo"); BigDecimal
					 * payRefDExchRate = rsDoc2Doc.getBigDecimal("exch_rate");
					 * String payRefDTxRefNo = rsDoc2Doc.getString("txrefno");
					 * String payRefDTxInfo = rsDoc2Doc.getString("txinfo");
					 * String payRefTxType = rsDoc2Doc.getString("txType");
					 * BigDecimal payRefDTxExchRate =
					 * rsDoc2Doc.getBigDecimal("txexch_rate"); Timestamp
					 * payRefDTxDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("txdate"), "MM/dd/yy HH:mm:ss");
					 * Timestamp payDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("paydate"), "MM/dd/yy HH:mm:ss");
					 * BigDecimal payAmt = rsDoc2Doc.getBigDecimal("payamt");
					 * BigDecimal lPayAmt = rsDoc2Doc.getBigDecimal("lpayamt");
					 * BigDecimal payRefDLTxAmt =
					 * rsDoc2Doc.getBigDecimal("ltxamt"); String posted =
					 * rsDoc2Doc.getString("posted"); String mthEnd =
					 * rsDoc2Doc.getString("mthend"); String payRefDCustId =
					 * rsDoc2Doc.getString("custId");
					 *  // Find the Invoice PKID corresponding to txRefNo
					 * findInvStmt.setString(1, payRefDTxRefNo); ResultSet
					 * rsFindInv = findInvStmt.executeQuery(); Long thisInvId =
					 * null; if(rsFindInv.next()) { thisInvId = new
					 * Long(rsFindInv.getLong("pkid"));
					 *  // Create the DocLink DocLink newDocLink =
					 * DocLinkNut.getHome().create( DocLinkBean.NS_CUSTOMER, "",
					 * DocLinkBean.RELTYPE_PAYMENT, ReceiptBean.TABLENAME,
					 * newRcptId, InvoiceBean.TABLENAME, thisInvId, thisCurr,
					 * lPayAmt, "", payAmt, "", TimeFormat.getTimestamp(),
					 * usrid); } else { //throw new Exception("Could not find
					 * Invoice " + payRefDTxRefNo); Log.printDebug("Could not
					 * find Invoice " + payRefDTxRefNo); } findInvStmt.close(); }
					 * doc2DocStmt.close();
					 */
				} // end if(txType = P) // PAYMENT
				else if (txType.equals("C") && !txStatus.equals("D")) // for
																		// CreditNote
				{
					Log.printDebug("*** Detected a CreditNote ***");
					// Query for Details from CDHisD
					String txnDQuery = "select * from cdhisd where docref = '" + docNo + "'";
					Statement txnDStmt = con.createStatement();
					ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					String creditNoteRemarks = remarks;
					while (rsTxnD.next())
					{
						String glId = rsTxnD.getString("glid");
						String stkId = rsTxnD.getString("stkid");
						String desc1 = rsTxnD.getString("desc1");
						if (!stkId.equals(""))
						{
							creditNoteRemarks = creditNoteRemarks + "," + stkId;
						} else
						{
							// remarks = remarks + ", (GLID=" + glId
							remarks = remarks + " (DESC=" + desc1 + ")";
							// creditNoteRemarks = creditNoteRemarks + ",
							// (GLID=" + glId
							creditNoteRemarks = creditNoteRemarks + " (DESC=" + desc1 + ")";
						}
						// Truncate remarks & creditNoteRemarks to 500 char
						if (remarks.length() > NominalAccountBean.MAX_LEN_REMARKS)
						{
							remarks = remarks.substring(0, NominalAccountBean.MAX_LEN_REMARKS);
						}
						if (creditNoteRemarks.length() > GenericStmtBean.MAX_LEN_REMARKS)
						{
							creditNoteRemarks = creditNoteRemarks.substring(0, GenericStmtBean.MAX_LEN_REMARKS);
						}
					}
					txnDStmt.close();
					NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER,
					// iDefPCCenterId, thisCustAcc.getPkid(), thisCurr);
							iDefPCCenterId, thisCustAcc.getPkid(), "MYR");
					if (naObj == null)
					{
						naObj = new NominalAccountObject();
						// naObj.pkid = new Integer("0");
						// naObj.code = new String("not_used");
						naObj.namespace = NominalAccountBean.NS_CUSTOMER;
						naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
						naObj.foreignKey = thisCustAcc.getPkid();
						naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
						// naObj.currency = thisCurr;
						naObj.currency = "MYR";
						// naObj.amount = naObj.amount.add(lTxAmt);
						naObj.remarks = remarks;
						naObj.accPCCenterId = iDefPCCenterId;
						naObj.state = NominalAccountBean.STATE_CREATED;
						naObj.status = NominalAccountBean.STATUS_ACTIVE;
						naObj.lastUpdate = TimeFormat.getTimestamp();
						naObj.userIdUpdate = usrid;
						NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
					}
					/*
					 * // if derivedLAmt = 0, take the sum from payrefd if
					 * (derivedLAmt.signum() == 0) { derivedLAmt =
					 * getPaymentSum(con, docNo); Log.printVerbose(docNo + ":
					 * derivedLAmt = " + derivedLAmt); }
					 *  // Create a Credit Note Long newCNId = createCreditNote(
					 * naObj.pkid, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
					 * GLCodeBean.CASH_DISCOUNT, txDate, InvoiceBean.TABLENAME,
					 * new Long(0), docNo, creditNoteRemarks,
					 * TimeFormat.getTimestamp(), usrid, derivedLAmt);
					 */
					// Alex (07/03) - must get from payrefd ALL THE TIME,
					// since we don't know if the payment if for multiple
					// customers
					// Vector vecPayment = getPaymentSum(con, docNo, thisCurr,
					// remarks,usrid);
					Vector vecPayment = getPaymentSum(con, docNo, "MYR", remarks, usrid);
					if (vecPayment.size() == 0)
					{
						Long newCNId = createCreditNote(naObj.pkid, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
								GLCodeBean.CASH_DISCOUNT, txDate, InvoiceBean.TABLENAME, new Long(0), docNo,
								creditNoteRemarks, TimeFormat.getTimestamp(),
								// usrid, thisCurr, lCreditAmt);
								usrid, "MYR", lCreditAmt);
					} else
					{
						for (int pymtCnt = 0; pymtCnt < vecPayment.size(); pymtCnt++)
						{
							Log.printVerbose("*** pymtCnt = " + pymtCnt + " ***");
							Payment thisPymt = (Payment) vecPayment.get(pymtCnt);
							// Create a Credit Note
							Long newCNId = createCreditNote(
							// naObj.pkid,
							// GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
									thisPymt.nomAccId, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
									GLCodeBean.CASH_DISCOUNT, txDate, InvoiceBean.TABLENAME, new Long(0), docNo,
									creditNoteRemarks, TimeFormat.getTimestamp(),
									// usrid, thisCurr, thisPymt.amt);
									usrid, "MYR", thisPymt.amt);
						} // end for
					} // end if (vecPayment.size() == 0)
					/*
					 * // Find the invoices mapped to this receipt String
					 * doc2DocQ = "select * from payrefd where payno = '" +
					 * docNo + "'"; Statement doc2DocStmt =
					 * con.createStatement(); ResultSet rsDoc2Doc =
					 * doc2DocStmt.executeQuery(doc2DocQ);
					 * 
					 * String findInvQ = "select pkid from cust_invoice_index
					 * where remarks ~ ?"; PreparedStatement findInvStmt =
					 * jbossCon.prepareStatement(findInvQ);
					 * 
					 * while (rsDoc2Doc.next()) { String payNo =
					 * rsDoc2Doc.getString("payno"); String payType =
					 * rsDoc2Doc.getString("paytype"); String payInfo =
					 * rsDoc2Doc.getString("payinfo"); BigDecimal
					 * payRefDExchRate = rsDoc2Doc.getBigDecimal("exch_rate");
					 * String payRefDTxRefNo = rsDoc2Doc.getString("txrefno");
					 * String payRefDTxInfo = rsDoc2Doc.getString("txinfo");
					 * String payRefTxType = rsDoc2Doc.getString("txType");
					 * BigDecimal payRefDTxExchRate =
					 * rsDoc2Doc.getBigDecimal("txexch_rate"); Timestamp
					 * payRefDTxDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("txdate"), "MM/dd/yy HH:mm:ss");
					 * Timestamp payDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("paydate"), "MM/dd/yy HH:mm:ss");
					 * BigDecimal payAmt = rsDoc2Doc.getBigDecimal("payamt");
					 * BigDecimal lPayAmt = rsDoc2Doc.getBigDecimal("lpayamt");
					 * BigDecimal payRefDLTxAmt =
					 * rsDoc2Doc.getBigDecimal("ltxamt"); String posted =
					 * rsDoc2Doc.getString("posted"); String mthEnd =
					 * rsDoc2Doc.getString("mthend"); String payRefDCustId =
					 * rsDoc2Doc.getString("custId");
					 *  // Find the Invoice PKID corresponding to txRefNo
					 * findInvStmt.setString(1, payRefDTxRefNo); ResultSet
					 * rsFindInv = findInvStmt.executeQuery(); Long thisInvId =
					 * null; if(rsFindInv.next()) { thisInvId = new
					 * Long(rsFindInv.getLong("pkid"));
					 *  // Create the DocLink DocLink newDocLink =
					 * DocLinkNut.getHome().create( DocLinkBean.NS_CUSTOMER, "",
					 * DocLinkBean.RELTYPE_CN, GenericStmtBean.TABLENAME,
					 * newCNId, InvoiceBean.TABLENAME, thisInvId, thisCurr,
					 * lPayAmt, "", payAmt, "", TimeFormat.getTimestamp(),
					 * usrid); } else { //throw new Exception("Could not find
					 * Invoice " + payRefDTxRefNo); Log.printDebug("Could not
					 * find Invoice " + payRefDTxRefNo); } findInvStmt.close(); }
					 * doc2DocStmt.close();
					 */
				} // end if(txType = C) // CreditNote
				else if (txType.equals("D") && !txStatus.equals("D")) // for
																		// DebitNote
				{
					Log.printDebug("*** Detected a DebitNote ***");
					// Query for Details from CDHisD
					String txnDQuery = "select * from cdhisd where docref = '" + docNo + "'";
					Statement txnDStmt = con.createStatement();
					ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					String debitNoteRemarks = remarks;
					while (rsTxnD.next())
					{
						String glId = rsTxnD.getString("glid");
						String stkId = rsTxnD.getString("stkid");
						String desc1 = rsTxnD.getString("desc1");
						if (!stkId.equals(""))
						{
							debitNoteRemarks = debitNoteRemarks + "," + stkId;
						} else
						{
							// remarks = remarks + ", (GLID=" + glId
							remarks = remarks + " (DESC=" + desc1 + ")";
							// debitNoteRemarks = debitNoteRemarks + ", (GLID="
							// + glId
							debitNoteRemarks = debitNoteRemarks + " (DESC=" + desc1 + ")";
						}
						// Truncate debitNoteRemarks to 500 char
						if (debitNoteRemarks.length() > GenericStmtBean.MAX_LEN_REMARKS)
						{
							debitNoteRemarks = debitNoteRemarks.substring(0, GenericStmtBean.MAX_LEN_REMARKS);
						}
					}
					txnDStmt.close();
					NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER,
					// iDefPCCenterId, thisCustAcc.getPkid(), thisCurr);
							iDefPCCenterId, thisCustAcc.getPkid(), "MYR");
					if (naObj == null)
					{
						naObj = new NominalAccountObject();
						// naObj.pkid = new Integer("0");
						// naObj.code = new String("not_used");
						naObj.namespace = NominalAccountBean.NS_CUSTOMER;
						naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
						naObj.foreignKey = thisCustAcc.getPkid();
						naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
						// naObj.currency = thisCurr;
						naObj.currency = "MYR";
						// naObj.amount = naObj.amount.add(lTxAmt);
						naObj.remarks = remarks;
						naObj.accPCCenterId = iDefPCCenterId;
						naObj.state = NominalAccountBean.STATE_CREATED;
						naObj.status = NominalAccountBean.STATUS_ACTIVE;
						naObj.lastUpdate = TimeFormat.getTimestamp();
						naObj.userIdUpdate = usrid;
						NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
					}
					// if derivedLAmt = 0, take the sum from payrefd
					if (derivedLAmt.signum() == 0)
					{
						// derivedLAmt = getRevPaymentSum(con, docNo);
						derivedLAmt = lDebitAmt;
						Log.printVerbose(docNo + ": derivedLAmt = " + derivedLAmt);
					}
					// Create a Debit Note
					Long newDNId = createDebitNote(naObj.pkid, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
							GLCodeBean.INTEREST_REVENUE, txDate, InvoiceBean.TABLENAME, new Long(0), docNo,
							debitNoteRemarks, TimeFormat.getTimestamp(),
							// usrid, thisCurr, derivedLAmt);
							usrid, "MYR", derivedLAmt);
					/*
					 * // Find the invoices mapped to this receipt String
					 * doc2DocQ = "select * from payrefd where payno = '" +
					 * docNo + "'"; Statement doc2DocStmt =
					 * con.createStatement(); ResultSet rsDoc2Doc =
					 * doc2DocStmt.executeQuery(doc2DocQ);
					 * 
					 * String findInvQ = "select pkid from cust_invoice_index
					 * where remarks ~ ?"; PreparedStatement findInvStmt =
					 * jbossCon.prepareStatement(findInvQ);
					 * 
					 * while (rsDoc2Doc.next()) { String payNo =
					 * rsDoc2Doc.getString("payno"); String payType =
					 * rsDoc2Doc.getString("paytype"); String payInfo =
					 * rsDoc2Doc.getString("payinfo"); BigDecimal
					 * payRefDExchRate = rsDoc2Doc.getBigDecimal("exch_rate");
					 * String payRefDTxRefNo = rsDoc2Doc.getString("txrefno");
					 * String payRefDTxInfo = rsDoc2Doc.getString("txinfo");
					 * String payRefTxType = rsDoc2Doc.getString("txType");
					 * BigDecimal payRefDTxExchRate =
					 * rsDoc2Doc.getBigDecimal("txexch_rate"); Timestamp
					 * payRefDTxDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("txdate"), "MM/dd/yy HH:mm:ss");
					 * Timestamp payDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("paydate"), "MM/dd/yy HH:mm:ss");
					 * BigDecimal payAmt = rsDoc2Doc.getBigDecimal("payamt");
					 * BigDecimal lPayAmt = rsDoc2Doc.getBigDecimal("lpayamt");
					 * BigDecimal payRefDLTxAmt =
					 * rsDoc2Doc.getBigDecimal("ltxamt"); String posted =
					 * rsDoc2Doc.getString("posted"); String mthEnd =
					 * rsDoc2Doc.getString("mthend"); String payRefDCustId =
					 * rsDoc2Doc.getString("custId");
					 *  // Find the Invoice PKID corresponding to txRefNo
					 * findInvStmt.setString(1, payRefDTxRefNo); ResultSet
					 * rsFindInv = findInvStmt.executeQuery(); Long thisInvId =
					 * null; if(rsFindInv.next()) { thisInvId = new
					 * Long(rsFindInv.getLong("pkid"));
					 *  // Create the DocLink DocLink newDocLink =
					 * DocLinkNut.getHome().create( DocLinkBean.NS_CUSTOMER, "",
					 * DocLinkBean.RELTYPE_DN, GenericStmtBean.TABLENAME,
					 * newDNId, InvoiceBean.TABLENAME, thisInvId, thisCurr,
					 * lPayAmt, "", payAmt, "", TimeFormat.getTimestamp(),
					 * usrid); } else { //throw new Exception("Could not find
					 * Invoice " + payRefDTxRefNo); Log.printDebug("Could not
					 * find Invoice " + payRefDTxRefNo); } findInvStmt.close(); }
					 * doc2DocStmt.close();
					 */
				} // end if(txType = D) // DebitNote
				else if (txType.equals("R") && !txStatus.equals("D")) // for
																		// ReversePayment
				{
					Log.printDebug("*** Detected a ReversePayment ***");
					// Query for Details from CDHisD
					String txnDQuery = "select * from cdhisd where docref = '" + docNo + "'";
					Statement txnDStmt = con.createStatement();
					ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					while (rsTxnD.next())
					{
						String glId = rsTxnD.getString("glid");
						String desc1 = rsTxnD.getString("desc1");
						// remarks = remarks + ", (GLID=" + glId
						remarks = remarks + " (DESC=" + desc1 + ")";
					}
					txnDStmt.close();
					NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER,
					// iDefPCCenterId, thisCustAcc.getPkid(), thisCurr);
							iDefPCCenterId, thisCustAcc.getPkid(), "MYR");
					if (naObj == null)
					{
						naObj = new NominalAccountObject();
						// naObj.pkid = new Integer("0");
						// naObj.code = new String("not_used");
						naObj.namespace = NominalAccountBean.NS_CUSTOMER;
						naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
						naObj.foreignKey = thisCustAcc.getPkid();
						naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
						// naObj.currency = thisCurr;
						naObj.currency = "MYR";
						// naObj.amount = naObj.amount.add(lTxAmt);
						naObj.remarks = remarks;
						naObj.accPCCenterId = iDefPCCenterId;
						naObj.state = NominalAccountBean.STATE_CREATED;
						naObj.status = NominalAccountBean.STATUS_ACTIVE;
						naObj.lastUpdate = TimeFormat.getTimestamp();
						naObj.userIdUpdate = usrid;
						NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
					}
					/*
					 * // if derivedLAmt = 0, take the sum from payrefd if
					 * (derivedLAmt.signum() == 0) { //derivedLAmt =
					 * getRevPaymentSum(con, docNo); derivedLAmt = lDebitAmt;
					 * Log.printVerbose(docNo + ": derivedLAmt = " +
					 * derivedLAmt); }
					 */
					Vector vecRevPayment = getRevPaymentSum(con, docNo, "MYR", remarks, usrid);
					if (vecRevPayment.size() == 0)
					{
						// Create a ReversePayment
						Long newRPId = createReversePayment(GenericStmtBean.STMT_TYPE_REVERSE_PAYMENT, naObj.pkid,
								iDefCashAccId, "MYR", // thisCurr,
								derivedLAmt, "", remarks, "", txDate, "", new Long(0), usrid);
					} else
					{
						for (int pymtCnt = 0; pymtCnt < vecRevPayment.size(); pymtCnt++)
						{
							Log.printVerbose("*** pymtCnt = " + pymtCnt + " ***");
							Payment thisPymt = (Payment) vecRevPayment.get(pymtCnt);
							// Create a ReversePayment
							Long newRPId = createReversePayment(GenericStmtBean.STMT_TYPE_REVERSE_PAYMENT,
									thisPymt.nomAccId, // naObj.pkid,
									iDefCashAccId, "MYR", // thisCurr,
									thisPymt.amt, // derivedLAmt,
									"", remarks, "", txDate, "", new Long(0), usrid);
						} // end for
					} // end if (vecPayment.size() == 0)
					// Derive the receipt mapped to this reverse payment
					// Note: Only deriving for docno that is in the form
					// R/OR#####
					Log.printVerbose("*** reversePaymentRef = " + docNo + "***");
					/*
					 * if (docNo.startsWith("R/OR")) { String receiptRef =
					 * docNo.substring(2); Log.printVerbose("*** receiptRef = " +
					 * receiptRef + "***");
					 * 
					 * String findRcptQ = "select pkid from cust_receipt_index
					 * where payment_remarks ~ " + "'" + receiptRef + "'";
					 * Statement findRcptStmt = jbossCon.createStatement();
					 * ResultSet rsFindRcpt =
					 * findRcptStmt.executeQuery(findRcptQ);
					 * 
					 * if(rsFindRcpt.next()) { Long thisRcptId = new
					 * Long(rsFindRcpt.getLong("pkid"));
					 *  // Create the DocLink DocLink newDocLink =
					 * DocLinkNut.getHome().create( DocLinkBean.NS_CUSTOMER, "",
					 * DocLinkBean.RELTYPE_REV_PAYMENT,
					 * GenericStmtBean.TABLENAME, newRPId,
					 * ReceiptBean.TABLENAME, thisRcptId, thisCurr, lTxAmt, "",
					 * txAmt, "", TimeFormat.getTimestamp(), usrid); }
					 * findRcptStmt.close(); }
					 */
				} // end if(txType = R) // ReversePayment
				else
				{
					Log.printDebug("*** UNKNOWN txtype ***");
				}
			} // end while(rsCustTxn.next())
			custTxnStmt.close();
			/*******************************************************************
			 * FIXES AFTER POPULATION OF DOC INDICES
			 ******************************************************************/
			// 1. Fix all transactions that had gainloseamt != 0 owing to
			// differing exch_rates
			// fixExRate(con, jbossCon, usrid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while converting Topcon DB: " + ex.getMessage());
		}
		Log.printVerbose("***** END: MIGRATION OF TOPCON DB *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "CREATE DOC INDEX");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		// Clean up curTask;
		curTask = null;
		return new ActionRouter("test-migrate-topcondb-page");
	}

	private Integer createNewSRep(String srId, String srDesc) throws Exception
	{
		// Create the user
		Integer newUsrId = UserNut.getUserId(srId);
		User newUsr = null;
		if (newUsrId != null)
		{
			return newUsrId;
			// newUsr = UserNut.getHandle(newUsrId);
		} else
		{
			newUsr = UserNut.getHome().create(srId, srId, srDesc, "");
		}
		// Create the user details
		Collection colUsrDet = UserDetailsNut.getCollectionByUserId(newUsr.getUserId());
		if (colUsrDet == null || colUsrDet.isEmpty())
		{
			UserDetails thisUserD = UserDetailsNut.getHome().create(newUsr.getUserId(), Calendar.getInstance(), "", "",
					"", "", Calendar.getInstance());
		}
		// Assign a default role for the user
		Collection colUsrRole = ActionDo.getUserRoleHome().findUserRolesGiven("userid", newUsr.getUserId().toString());
		if (colUsrRole == null || colUsrRole.isEmpty())
		{
			ActionDo.getUserRoleHome().create(RoleBean.ROLEID_DEVELOPER, newUsr.getUserId(), Calendar.getInstance());
		}
		return newUsr.getUserId();
	}

	private Integer getUOM(String uom) throws Exception
	{
		Integer thisUOM = new Integer(ItemBean.UOM_NONE);
		if (uom.equals("PCE") || uom.equals("PCS"))
			thisUOM = new Integer(ItemBean.UOM_PCS);
		else if (uom.equals("ROLL"))
			thisUOM = new Integer(ItemBean.UOM_ROLL);
		else if (uom.equals("SET") || uom.equals("SETS") || uom.equals("SSET"))
			thisUOM = new Integer(ItemBean.UOM_SET);
		else if (uom.equals("UNIT") || uom.equals("NOS"))
			thisUOM = new Integer(ItemBean.UOM_UNIT);
		return thisUOM;
	}

	private Long createCustReceipt(Integer naPkid, Integer caPkid, Timestamp tsReceiptDate, Timestamp tsEffFromDate,
			Timestamp tsEffToDate, String currency, BigDecimal bdPaymentAmt, String strPaymentMethod,
			String strPaymentRmks,
			// String strGLCodeCredit,
			// String strGLCodeDebit,
			String strChequeNumber,
			// String strCashAccount,
			Integer iUsrId, Timestamp tsCreate) throws Exception
	{
		Long salesTxnId = new Long("0");
		String strAmountStr = CurrencyFormat.toWords(bdPaymentAmt, "", "");
		strAmountStr = strAmountStr + " ONLY";
		// / Get objects based on parameters above
		NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
		CashAccountObject caObj = CashAccountNut.getObject(caPkid);
		if (defBranchObj == null)
			defBranchObj = BranchNut.getObject(iDefSvcCtrId);
		/*
		 * /// 1) create a printable Cust Receipt Object ReceiptObject cRcptObj =
		 * new ReceiptObject(); cRcptObj.salesTxnId = salesTxnId;
		 * cRcptObj.custAccount = naObj.foreignKey; cRcptObj.intReserved1 = new
		 * Integer("0"); cRcptObj.strReserved1 = ""; //cRcptObj.currency =
		 * naObj.currency; cRcptObj.currency = currency; cRcptObj.amountStr =
		 * strAmountStr; cRcptObj.paymentAmount = bdPaymentAmt;
		 * cRcptObj.paymentMethod = strPaymentMethod; cRcptObj.paymentTime =
		 * tsReceiptDate; cRcptObj.chequeNumber = strChequeNumber;
		 * cRcptObj.bankId = caPkid; cRcptObj.paymentRemarks = strPaymentRmks;
		 * cRcptObj.glCode = GLCodeBean.GENERAL_SALES; cRcptObj.state =
		 * ReceiptBean.ST_CREATED; cRcptObj.status = ReceiptBean.STATUS_ACTIVE;
		 * cRcptObj.lastUpdate = tsCreate; cRcptObj.userIdUpdate = iUsrId;
		 * 
		 * Receipt sPayEJB = ReceiptNut.fnCreate(cRcptObj);
		 */
		// // create the receipt
		OfficialReceiptObject receiptObj = new OfficialReceiptObject();
		receiptObj.entityTable = CustAccountBean.TABLENAME;
		receiptObj.entityKey = naObj.foreignKey;
		receiptObj.entityName = CustAccountNut.getHandle(receiptObj.entityKey).getName();
		receiptObj.currency = currency;
		receiptObj.amount = bdPaymentAmt;
		receiptObj.paymentTime = tsReceiptDate;
		receiptObj.paymentMethod = strPaymentMethod;
		receiptObj.paymentRemarks = strPaymentRmks;
		receiptObj.chequeNumber = strChequeNumber;
		receiptObj.lastUpdate = TimeFormat.getTimestamp();
		receiptObj.userIdUpdate = iUsrId;
		receiptObj.cbCash = defBranchObj.cashbookCash;
		receiptObj.cbCard = defBranchObj.cashbookCard;
		receiptObj.cbCheque = defBranchObj.cashbookCheque;
		receiptObj.cbPDCheque = defBranchObj.cashbookPDCheque;
		if (strPaymentMethod.equals("cheque"))
		{
			receiptObj.amountCheque = bdPaymentAmt;
		} else
		{
			receiptObj.amountCash = bdPaymentAmt;
		}
		// receiptObj.amountCard = this.amountCard;
		// receiptObj.amountPDCheque = this.amountPDCheque;
		// if(this.amountPDCheque.signum()>0)
		// { receiptObj.chequeNumberPD = this.chequeNumberPD;}
		// receiptObj.datePDCheque = this.pdChequeDate;
		receiptObj.branch = defBranchObj.pkid;
		receiptObj.pcCenter = iDefPCCenterId;
		OfficialReceipt offRctEJB = OfficialReceiptNut.fnCreate(receiptObj);
		Log.printVerbose(receiptObj.toString());
		if (offRctEJB == null)
		{
			throw new Exception("Failed to create Receipt!!");
		}
		// / 2) create an entry in the nominal account transaction
		/*
		 * NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		 * natObj.nominalAccount = naObj.pkid; natObj.foreignTable =
		 * NominalAccountTxnBean.FT_CUST_RECEIPT; natObj.foreignKey =
		 * receiptObj.pkid; natObj.code = "not_used"; natObj.info1 = "";
		 * natObj.description = strPaymentRmks; natObj.glCodeCredit =
		 * GLCodeBean.ACC_RECEIVABLE; natObj.glCodeDebit = caObj.accountType;
		 * natObj.currency = naObj.currency; natObj.amount =
		 * bdPaymentAmt.negate(); natObj.timeOption1 =
		 * NominalAccountTxnBean.TIME_STMT; natObj.timeParam1 = tsReceiptDate;
		 * natObj.timeOption2 = NominalAccountTxnBean.TIME_NA; natObj.timeParam2 =
		 * tsReceiptDate; natObj.state = NominalAccountTxnBean.ST_CREATED;
		 * natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
		 * natObj.lastUpdate = tsCreate; natObj.userIdUpdate = iUsrId;
		 * NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
		 * 
		 * if(natEJB == null) { try { offRctEJB.remove();} catch(Exception ex) {
		 * ex.printStackTrace();} throw new Exception("Failed to create NAT"); }
		 * /// 4) update cash account transactions CashAccTxnObject catObj = new
		 * CashAccTxnObject(); catObj.txnCode = CashAccTxnBean.TXN_CODE_DEFAULT;
		 * catObj.glCodeCredit = GLCodeBean.ACC_RECEIVABLE; catObj.glCodeDebit =
		 * caObj.accountType; catObj.personInCharge = iUsrId; catObj.accFrom =
		 * new Integer("0");// another known bank account catObj.accTo =
		 * caObj.pkId; catObj.foreignTable = CashAccTxnBean.FT_CUST_RECEIPT;
		 * catObj.foreignKey = receiptObj.pkid; catObj.currency =
		 * naObj.currency; catObj.amount = bdPaymentAmt; catObj.txnTime =
		 * tsReceiptDate; catObj.remarks = strPaymentRmks; catObj.info1 = "";
		 * catObj.info2 = ""; catObj.state = CashAccTxnBean.ST_CREATED;
		 * catObj.status = CashAccTxnBean.STATUS_ACTIVE; catObj.lastUpdate =
		 * tsCreate; catObj.userIdUpdate = iUsrId; catObj.pcCenter =
		 * caObj.pcCenter; CashAccTxn catEJB = CashAccTxnNut.fnCreate(catObj);
		 * /// 5) update cash account balance // no need to set the balance !!!
		 * hurray !!!!
		 * 
		 * /// 3) update the nominal account NominalAccount naEJB =
		 * NominalAccountNut.getHandle(naObj.pkid); BigDecimal bdLatestBal =
		 * naEJB.getAmount(); bdLatestBal = bdLatestBal.add(natObj.amount);
		 * naObj.amount = bdLatestBal;
		 */
		return receiptObj.pkid;
	}

	private Long createCreditNote(Integer naPkid, String stmtType, String glCodeDebit, Timestamp stmtDate,
			String fStmtTable, Long fStmtKey, String refNo, String remarks, Timestamp timeUpdate, Integer userId,
			String currency, BigDecimal amount) throws Exception
	{
		// try
		// {
		String strErrMsg = null;
		/*
		 * Integer naPkid = new Integer(req.getParameter("naPkid")); String
		 * stmtType = req.getParameter("stmtType"); String glCodeDebit =
		 * req.getParameter("glCodeDebit"); Timestamp stmtDate =
		 * TimeFormat.createTimestamp( req.getParameter("stmtDate")); String
		 * fStmtTable = req.getParameter("fStmtTable"); Long fStmtKey = new
		 * Long(req.getParameter("fStmtKey")); String refNo =
		 * req.getParameter("refNo"); String remarks =
		 * req.getParameter("remarks"); Timestamp timeUpdate =
		 * TimeFormat.getTimestamp(); Integer userId =
		 * UserNut.getUserId(req.getParameter("userName")); BigDecimal amount =
		 * new BigDecimal(req.getParameter("amount"));
		 */
		// / checking input variables
		if (naPkid == null)
			throw new Exception(" Invalid Nominal Account PKID ");
		NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
		GenericStmtObject gsObj = new GenericStmtObject();
		gsObj.namespace = GenericStmtBean.NS_CUSTOMER;
		gsObj.dateStmt = stmtDate;
		// // assuming Credit Note here!!!!
		gsObj.glCodeDebit = glCodeDebit;
		gsObj.glCodeCredit = GLCodeBean.ACC_RECEIVABLE;
		// gsObj.currency = naObj.currency;
		gsObj.currency = currency; // don't inherit from NominalAccount
		gsObj.amount = amount;
		gsObj.dateDue = stmtDate;
		gsObj.foreignEntityTable = naObj.foreignTable;
		gsObj.foreignEntityKey = naObj.foreignKey;
		gsObj.pcCenter = naObj.accPCCenterId;
		gsObj.stmtType = stmtType;
		gsObj.foreignStmtTable = fStmtTable;
		gsObj.foreignStmtKey = fStmtKey;
		gsObj.referenceNo = refNo;
		// gsObj.remarks = remarks;
		gsObj.remarks = "AUTO-CN: " + remarks;
		gsObj.dateStart = gsObj.dateStmt;
		gsObj.dateEnd = gsObj.dateStmt;
		gsObj.dateDue = gsObj.dateStmt;
		gsObj.dateCreated = gsObj.dateStmt;
		gsObj.dateApproved = gsObj.dateStmt;
		gsObj.dateVerified = gsObj.dateStmt;
		gsObj.dateVerified = gsObj.dateStmt;
		gsObj.dateUpdate = timeUpdate;
		gsObj.userIdCreate = userId;
		gsObj.userIdPIC = userId;
		gsObj.userIdApprove = userId;
		gsObj.userIdVerified = userId;
		gsObj.userIdUpdate = userId;
		// create a nominal account txn for this generic stmt
		NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		natObj.nominalAccount = naObj.pkid;
		natObj.foreignTable = GenericStmtBean.TABLENAME;
		natObj.description = gsObj.remarks;
		natObj.glCodeDebit = gsObj.glCodeDebit;
		natObj.glCodeCredit = gsObj.glCodeCredit;
		natObj.currency = gsObj.currency;
		natObj.amount = gsObj.amount.negate();
		natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
		natObj.timeParam1 = gsObj.dateStmt;
		natObj.state = NominalAccountTxnBean.ST_ACTUAL;
		natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
		natObj.lastUpdate = TimeFormat.getTimestamp();
		natObj.userIdUpdate = gsObj.userIdCreate;
		GenericStmt gsEJB = GenericStmtNut.fnCreate(gsObj);
		if (gsEJB == null)
		{
			throw new Exception(" Unable to create Generic Stmt Bean ");
		}
		natObj.foreignKey = gsObj.pkid;
		NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
		if (natEJB == null)
		{
			try
			{
				gsEJB.remove();
			} catch (Exception ex)
			{
				ex.printStackTrace();
				throw new Exception(" Unable to create Nominal Account Txn ");
			}
		}
		naObj.amount = naObj.amount.add(natObj.amount);
		return gsObj.pkid;
	}

	private Long createDebitNote(Integer naPkid, String stmtType, String glCodeCredit, Timestamp stmtDate,
			String fStmtTable, Long fStmtKey, String refNo, String remarks, Timestamp timeUpdate, Integer userId,
			String currency, BigDecimal amount) throws Exception
	{
		String strErrMsg = null;
		// / checking input variables
		if (naPkid == null)
			throw new Exception(" Invalid Nominal Account PKID ");
		NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
		GenericStmtObject gsObj = new GenericStmtObject();
		gsObj.namespace = GenericStmtBean.NS_CUSTOMER;
		gsObj.dateStmt = stmtDate;
		// // assuming Credit Note here!!!!
		gsObj.glCodeDebit = GLCodeBean.ACC_RECEIVABLE;
		gsObj.glCodeCredit = glCodeCredit;
		// gsObj.currency = naObj.currency;
		gsObj.currency = currency;
		gsObj.amount = amount;
		gsObj.dateDue = stmtDate;
		gsObj.foreignEntityTable = naObj.foreignTable;
		gsObj.foreignEntityKey = naObj.foreignKey;
		gsObj.pcCenter = naObj.accPCCenterId;
		gsObj.stmtType = stmtType;
		gsObj.foreignStmtTable = fStmtTable;
		gsObj.foreignStmtKey = fStmtKey;
		gsObj.referenceNo = refNo;
		// gsObj.remarks = remarks;
		gsObj.remarks = "AUTO-DN: " + remarks;
		gsObj.dateStart = gsObj.dateStmt;
		gsObj.dateEnd = gsObj.dateStmt;
		gsObj.dateDue = gsObj.dateStmt;
		gsObj.dateCreated = gsObj.dateStmt;
		gsObj.dateApproved = gsObj.dateStmt;
		gsObj.dateVerified = gsObj.dateStmt;
		gsObj.dateVerified = gsObj.dateStmt;
		gsObj.dateUpdate = timeUpdate;
		gsObj.userIdCreate = userId;
		gsObj.userIdPIC = userId;
		gsObj.userIdApprove = userId;
		gsObj.userIdVerified = userId;
		gsObj.userIdUpdate = userId;
		// create a nominal account txn for this generic stmt
		NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		natObj.nominalAccount = naObj.pkid;
		natObj.foreignTable = GenericStmtBean.TABLENAME;
		natObj.description = gsObj.remarks;
		natObj.glCodeDebit = gsObj.glCodeDebit;
		natObj.glCodeCredit = gsObj.glCodeCredit;
		natObj.currency = gsObj.currency;
		// natObj.amount = gsObj.amount.negate();
		natObj.amount = gsObj.amount;
		natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
		natObj.timeParam1 = gsObj.dateStmt;
		natObj.state = NominalAccountTxnBean.ST_ACTUAL;
		natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
		natObj.lastUpdate = TimeFormat.getTimestamp();
		natObj.userIdUpdate = gsObj.userIdCreate;
		GenericStmt gsEJB = GenericStmtNut.fnCreate(gsObj);
		if (gsEJB == null)
		{
			throw new Exception(" Unable to create Generic Stmt Bean ");
		}
		natObj.foreignKey = gsObj.pkid;
		NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
		if (natEJB == null)
		{
			try
			{
				gsEJB.remove();
			} catch (Exception ex)
			{
				ex.printStackTrace();
				throw new Exception(" Unable to create Nominal Account Txn ");
			}
		}
		naObj.amount = naObj.amount.add(natObj.amount);
		return gsObj.pkid;
	}

	private Long createReversePayment(String strStmtType, Integer iNominalAccId, Integer iCashAccountId,
			String currency, BigDecimal bdPaymentAmt, String strChequeCreditCardNo, String strRemarks, String strInfo1,
			Timestamp tsDateStmt, String strForeignStmtTable, Long iSettleStmtId, Integer usrid) throws Exception
	{
		String strErrMsg = null;
		// first of all, get the for parameters
		// String strStmtType = (String) req.getParameter("stmtType");
		// String strNominalAcc = (String)req.getParameter("nominalAcc");
		// String strCashAccount = (String)req.getParameter("cashAccount");
		// String strAmount = (String)req.getParameter("amount");
		// BigDecimal bdPaymentAmt= new BigDecimal(strAmount);
		// String strAmtInWords = (String)req.getParameter("amtInWords");
		String strAmtInWords = CurrencyFormat.toWords(bdPaymentAmt, "", "");
		strAmtInWords = strAmtInWords + " ONLY";
		/*
		 * String strChequeCreditCardNo = (String)
		 * req.getParameter("chequeCreditCardNo"); String strRemarks =
		 * (String)req.getParameter("remarks"); String strInfo1 =
		 * (String)req.getParameter("info1");
		 * 
		 * String strDateStmt = (String) req.getParameter("dateStmt");
		 * 
		 * String foreignStmtTable = (String)
		 * req.getParameter("foreignStmtTable"); String strSettleStmtId =
		 * (String) req.getParameter("foreignStmtKey"); String userName =
		 * (String) req.getParameter("userName");
		 */
		// Get the cash account object
		CashAccountObject caObj = null;
		try
		{
			caObj = CashAccountNut.getObject(
			// new Integer(strCashAccount));
					iCashAccountId);
		} catch (Exception ex)
		{ /* do nothing */
		}
		if (caObj == null)
		{
			throw new Exception("Invalid Cash Account Object ");
		}
		// Get the nominal account object
		NominalAccountObject naObj = null;
		try
		{
			naObj = NominalAccountNut.getObject(
			// new Integer(strNominalAcc));
					iNominalAccId);
		} catch (Exception ex)
		{ /* do nothing */
		}
		if (naObj == null)
		{
			throw new Exception("Error fetching Nominal Account Object ");
		}
		GenericStmtObject gsObj = new GenericStmtObject();
		try
		{
			gsObj.namespace = GenericStmtBean.NS_CUSTOMER;
			gsObj.pcCenter = naObj.accPCCenterId;
			gsObj.glCodeDebit = GLCodeBean.ACC_RECEIVABLE;
			gsObj.glCodeCredit = caObj.accountType;
			// gsObj.currency = naObj.currency;
			gsObj.currency = currency;
			gsObj.amount = bdPaymentAmt;
			gsObj.cashFrom = caObj.pkId;
			gsObj.cashTo = caObj.pkId;
			gsObj.stmtType = strStmtType;
			// gsObj.foreignStmtTable = GenericStmtBean.TABLENAME;
			gsObj.foreignStmtTable = strForeignStmtTable;
			gsObj.foreignStmtKey = iSettleStmtId;
			gsObj.chequeCreditCardNo = strChequeCreditCardNo;
			gsObj.remarks = strRemarks;
			gsObj.info1 = strInfo1;
			gsObj.info2 = strAmtInWords;
			gsObj.nominalAccount = naObj.pkid;
			gsObj.foreignEntityTable = GenericEntityAccountBean.TABLENAME;
			gsObj.foreignEntityKey = naObj.foreignKey;
			gsObj.dateStmt = tsDateStmt;
			gsObj.dateStart = tsDateStmt;
			gsObj.dateEnd = tsDateStmt;
			gsObj.dateDue = tsDateStmt;
			gsObj.dateCreated = TimeFormat.getTimestamp();
			gsObj.dateApproved = TimeFormat.getTimestamp();
			gsObj.dateVerified = TimeFormat.getTimestamp();
			gsObj.dateUpdate = TimeFormat.getTimestamp();
			gsObj.userIdCreate = usrid;
			gsObj.userIdPIC = gsObj.userIdCreate;
			gsObj.userIdApprove = gsObj.userIdCreate;
			gsObj.userIdVerified = gsObj.userIdCreate;
			gsObj.userIdUpdate = gsObj.userIdCreate;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception(" Unable to create GenericStmtObject ");
		}
		// update the cash account txn
		CashAccTxnObject catObjF = new CashAccTxnObject();
		catObjF.txnCode = CashAccTxnBean.TXN_CODE_DEFAULT;
		catObjF.glCodeDebit = gsObj.glCodeDebit;
		catObjF.glCodeCredit = gsObj.glCodeCredit;
		catObjF.personInCharge = gsObj.userIdCreate;
		// catObjF.accFrom = caObjTo.pkId;
		catObjF.accTo = caObj.pkId;
		catObjF.foreignTable = CashAccTxnBean.FT_GENERIC_STMT;
		catObjF.foreignKey = gsObj.pkid;
		catObjF.currency = gsObj.currency;
		catObjF.amount = gsObj.amount.negate();
		catObjF.txnTime = gsObj.dateStmt;
		catObjF.remarks = gsObj.remarks;
		catObjF.state = CashAccTxnBean.ST_CREATED;
		catObjF.status = CashAccTxnBean.STATUS_ACTIVE;
		catObjF.lastUpdate = TimeFormat.getTimestamp();
		catObjF.userIdUpdate = gsObj.userIdCreate;
		catObjF.pcCenter = caObj.pcCenter;
		CashAccTxnNut.fnCreate(catObjF);
		// create a nominal account txn for this generic stmt
		NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		natObj.nominalAccount = naObj.pkid;
		natObj.foreignTable = GenericStmtBean.TABLENAME;
		// the natObj.foreignKey will be assigned later after gsObj
		// is created and a pkid is obtained
		natObj.description = gsObj.remarks;
		natObj.glCodeDebit = gsObj.glCodeDebit;
		natObj.glCodeCredit = gsObj.glCodeCredit;
		natObj.currency = gsObj.currency;
		natObj.amount = gsObj.amount;
		natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
		natObj.timeParam1 = gsObj.dateStmt;
		natObj.state = NominalAccountTxnBean.ST_ACTUAL;
		natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
		natObj.lastUpdate = TimeFormat.getTimestamp();
		natObj.userIdUpdate = gsObj.userIdCreate;
		GenericStmt gsEJB = GenericStmtNut.fnCreate(gsObj);
		if (gsEJB == null)
		{
			throw new Exception(" Unable to create Generic Stmt Bean ");
		}
		natObj.foreignKey = gsObj.pkid;
		NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
		if (natEJB == null)
		{
			try
			{
				gsEJB.remove();
			} catch (Exception ex)
			{
				ex.printStackTrace();
				throw new Exception(" Unable to create Nominal Account Txn ");
			}
		}
		naObj.amount = naObj.amount.add(natObj.amount);
		return gsObj.pkid;
	}

	private Vector getPaymentSum(Connection con, String payNo, String strCurr, String remarks, Integer usrid)
			throws Exception
	{
		// drop tmp table
		try
		{
			String dropTmpTable = "drop table tmp_payrefd";
			Statement dropTmpTableStmt = con.createStatement();
			dropTmpTableStmt.executeUpdate(dropTmpTable);
		} catch (Exception ex)
		{
			// ignore
		}
		String selectDistinct = "select distinct payno, txrefno, custid, lpayamt into tmp_payrefd "
				+ "from payrefd where payno = '" + payNo + "'";
		Log.printVerbose("selectDistinct = " + selectDistinct);
		String getPymtSumQ = "select custid, sum(lpayamt) from tmp_payrefd group by custid";
		Log.printVerbose("getPymtSumQ = " + getPymtSumQ);
		String strCustId = null;
		BigDecimal bdSumOfPayments = new BigDecimal(0);
		Vector vecRtn = new Vector();
		Statement selectDistinctStmt = con.createStatement();
		Statement getPymtSumStmt = con.createStatement();
		selectDistinctStmt.executeUpdate(selectDistinct);
		ResultSet rsGetPymtSum = getPymtSumStmt.executeQuery(getPymtSumQ);
		while (rsGetPymtSum.next())
		{
			Log.printVerbose("Found a hit ...");
			// try
			// {
			strCustId = rsGetPymtSum.getString(1);
			bdSumOfPayments = rsGetPymtSum.getBigDecimal(2);
			// derive the NominalAccount
			CustAccount thisCustAcc = CustAccountNut.getObjectByCode(strCustId);
			NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER, iDefPCCenterId,
					thisCustAcc.getPkid(), strCurr);
			if (naObj == null)
			{
				naObj = new NominalAccountObject();
				// naObj.pkid = new Integer("0");
				// naObj.code = new String("not_used");
				naObj.namespace = NominalAccountBean.NS_CUSTOMER;
				naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
				naObj.foreignKey = thisCustAcc.getPkid();
				naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
				naObj.currency = strCurr;
				// naObj.amount = naObj.amount.add(lTxAmt);
				naObj.remarks = remarks;
				naObj.accPCCenterId = iDefPCCenterId;
				naObj.state = NominalAccountBean.STATE_CREATED;
				naObj.status = NominalAccountBean.STATUS_ACTIVE;
				naObj.lastUpdate = TimeFormat.getTimestamp();
				naObj.userIdUpdate = usrid;
				NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
			}
			vecRtn.add(new Payment(naObj.pkid, bdSumOfPayments));
			// }
			// catch(Exception ex)
			// {
			// }
		} // end while
		selectDistinctStmt.close();
		getPymtSumStmt.close();
		return vecRtn;
	}

	private Vector getRevPaymentSum(Connection con, String docNo, String strCurr, String remarks, Integer usrid)
			throws Exception
	{
		String getPymtSumQ = "select custid, sum(lpayamt) from "
				+ "(select distinct payno, txrefno, custid, lpayamt from payrefd " + " where txrefno = '" + docNo
				+ "') as tmp_payrefd group by custid";
		Log.printVerbose("getRevPymtSumQ = " + getPymtSumQ);
		String strCustId = null;
		BigDecimal bdSumOfPayments = new BigDecimal(0);
		Vector vecRtn = new Vector();
		Statement getPymtSumStmt = con.createStatement();
		ResultSet rsGetPymtSum = getPymtSumStmt.executeQuery(getPymtSumQ);
		while (rsGetPymtSum.next())
		{
			Log.printVerbose("Found a hit ...");
			// try
			// {
			strCustId = rsGetPymtSum.getString(1);
			bdSumOfPayments = rsGetPymtSum.getBigDecimal(2);
			// derive the NominalAccount
			CustAccount thisCustAcc = CustAccountNut.getObjectByCode(strCustId);
			NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER, iDefPCCenterId,
					thisCustAcc.getPkid(), strCurr);
			if (naObj == null)
			{
				naObj = new NominalAccountObject();
				// naObj.pkid = new Integer("0");
				// naObj.code = new String("not_used");
				naObj.namespace = NominalAccountBean.NS_CUSTOMER;
				naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
				naObj.foreignKey = thisCustAcc.getPkid();
				naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
				naObj.currency = strCurr;
				// naObj.amount = naObj.amount.add(lTxAmt);
				naObj.remarks = remarks;
				naObj.accPCCenterId = iDefPCCenterId;
				naObj.state = NominalAccountBean.STATE_CREATED;
				naObj.status = NominalAccountBean.STATUS_ACTIVE;
				naObj.lastUpdate = TimeFormat.getTimestamp();
				naObj.userIdUpdate = usrid;
				NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
			}
			vecRtn.add(new Payment(naObj.pkid, bdSumOfPayments));
			// }
			// catch(Exception ex)
			// {
			// }
		} // end while
		getPymtSumStmt.close();
		return vecRtn;
	}

	/*
	 * private BigDecimal getPaymentSum(Connection con, String payNo) throws
	 * Exception { String getPymtSumQ = "select sum(lpayamt) from payrefd where
	 * payno = '" + payNo + "'";
	 * 
	 * BigDecimal bdSumOfPayments = new BigDecimal(0); Statement getPymtSumStmt =
	 * con.createStatement(); ResultSet rsGetPymtSum =
	 * getPymtSumStmt.executeQuery(getPymtSumQ); if (rsGetPymtSum.next()) { try {
	 * bdSumOfPayments = rsGetPymtSum.getBigDecimal(1); } catch(Exception ex) { }
	 * 
	 * if(bdSumOfPayments == null) bdSumOfPayments = new BigDecimal(0); }
	 * 
	 * return bdSumOfPayments; }
	 */
	/*
	 * private BigDecimal getRevPaymentSum(Connection con, String payNo) throws
	 * Exception { String getPymtSumQ = "select sum(lpayamt) from payrefd where
	 * txrefno = '" + payNo + "'";
	 * 
	 * BigDecimal bdSumOfPayments = new BigDecimal(0); Statement getPymtSumStmt =
	 * con.createStatement(); ResultSet rsGetPymtSum =
	 * getPymtSumStmt.executeQuery(getPymtSumQ); if (rsGetPymtSum.next()) { try {
	 * bdSumOfPayments = rsGetPymtSum.getBigDecimal(1); } catch(Exception ex) { }
	 * 
	 * if(bdSumOfPayments == null) bdSumOfPayments = new BigDecimal(0); }
	 * 
	 * return bdSumOfPayments; }
	 */
	private void fixExRate(Connection topconCon, Connection jbossCon, Integer usrid) throws Exception
	{
		String findInvQ = "select pkid from cust_invoice_index where remarks ~* ? ";
		PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		String findCurrQ = "select currid from cdhis where docno = ? ";
		PreparedStatement findCurrStmt = topconCon.prepareStatement(findCurrQ);
		String queryXRateGainLose = "select * from payrefd where gainloseamt != 0 ";
		Statement xRateGainLoseStmt = topconCon.createStatement();
		ResultSet rsXRateGainLose = xRateGainLoseStmt.executeQuery(queryXRateGainLose);
		int count = 0;
		while (rsXRateGainLose.next())
		{
			Log.printVerbose("*** Processing Txn " + ++count);
			String strPayNo = rsXRateGainLose.getString("payno");
			String strTxRefNo = rsXRateGainLose.getString("txrefno");
			String strCustCode = rsXRateGainLose.getString("custid");
			Timestamp payDate = TimeFormat.createTimeStamp(rsXRateGainLose.getString("paydate"), "MM/dd/yy HH:mm:ss");
			BigDecimal bdPayExchRate = rsXRateGainLose.getBigDecimal("exch_rate");
			BigDecimal bdTxExchRate = rsXRateGainLose.getBigDecimal("txexch_rate");
			BigDecimal bdGainLoseAmt = rsXRateGainLose.getBigDecimal("gainloseamt");
			// Get the Invoice Id
			Long invId = new Long(0);
			findInvStmt.setString(1, " = " + strTxRefNo + ")");
			ResultSet rsFindInv = findInvStmt.executeQuery();
			if (rsFindInv.next())
			{
				invId = new Long(rsFindInv.getLong("pkid"));
			}
			// Get the Customer Id
			Integer custId = null;
			findCustStmt.setString(1, strCustCode);
			ResultSet rsFindCust = findCustStmt.executeQuery();
			if (rsFindCust.next())
			{
				custId = new Integer(rsFindCust.getInt("pkid"));
			}
			// Get the Currency
			String strCurr = null;
			findCurrStmt.setString(1, strPayNo);
			ResultSet rsFindCurr = findCurrStmt.executeQuery();
			if (rsFindCurr.next())
			{
				strCurr = rsFindCurr.getString("currid");
				strCurr = (String) hmCurr.get(new Integer(strCurr));
			}
			// Get the Nominal Account
			NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER, iDefPCCenterId,
					custId, "MYR");
			if (naObj == null)
			{
				naObj = new NominalAccountObject();
				// naObj.pkid = new Integer("0");
				// naObj.code = new String("not_used");
				naObj.namespace = NominalAccountBean.NS_CUSTOMER;
				naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
				naObj.foreignKey = custId;
				naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
				// naObj.currency = thisCurr;
				naObj.currency = "MYR";
				// naObj.amount = naObj.amount.add(lTxAmt);
				// naObj.remarks = remarks;
				naObj.accPCCenterId = iDefPCCenterId;
				naObj.state = NominalAccountBean.STATE_CREATED;
				naObj.status = NominalAccountBean.STATUS_ACTIVE;
				naObj.lastUpdate = TimeFormat.getTimestamp();
				naObj.userIdUpdate = usrid;
				NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
			}
			String strRemarks = "EXCH_RATE OFFSET (txexch_rate=" + bdTxExchRate.toString() + ", pay_exch_rate="
					+ bdPayExchRate.toString() + ")";
			// if bdGainLoseAmt < 0, means we need to CN to offset the balance
			// if bdGainLoseAmt > 0, means we need to DN to offset the balance
			if (bdGainLoseAmt.signum() < 0)
			{
				Log.printVerbose("*** " + count + ": Creating CN");
				Long newCNId = createCreditNote(naObj.pkid, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
						GLCodeBean.CASH_DISCOUNT, payDate, InvoiceBean.TABLENAME, invId, strPayNo, strRemarks,
						TimeFormat.getTimestamp(), usrid, strCurr, bdGainLoseAmt.abs());
			} else
			{
				// Create a Debit Note
				Log.printVerbose("*** " + count + ": Creating DN");
				Long newDNId = createDebitNote(naObj.pkid, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
						GLCodeBean.INTEREST_REVENUE, payDate, InvoiceBean.TABLENAME, invId, strPayNo, strRemarks,
						TimeFormat.getTimestamp(), usrid, strCurr, bdGainLoseAmt.abs());
			}
		} // end while
	} // end fixExRate
	// internal class
	class Payment
	{
		public BigDecimal amt;
		public Integer nomAccId;

		Payment(Integer nomAccId, BigDecimal amt)
		{
			this.amt = amt;
			this.nomAccId = nomAccId;
		}
	}
}
package com.vlee.servlet.test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vlee.ejb.accounting.BranchNut;
import com.vlee.ejb.accounting.BranchObject;
import com.vlee.ejb.accounting.CashAccTxnBean;
import com.vlee.ejb.accounting.CashAccTxnNut;
import com.vlee.ejb.accounting.CashAccTxnObject;
import com.vlee.ejb.accounting.CashAccountNut;
import com.vlee.ejb.accounting.CashAccountObject;
import com.vlee.ejb.accounting.GLCodeBean;
import com.vlee.ejb.accounting.GenericEntityAccountBean;
import com.vlee.ejb.accounting.GenericStmt;
import com.vlee.ejb.accounting.GenericStmtBean;
import com.vlee.ejb.accounting.GenericStmtNut;
import com.vlee.ejb.accounting.GenericStmtObject;
import com.vlee.ejb.accounting.NominalAccount;
import com.vlee.ejb.accounting.NominalAccountBean;
import com.vlee.ejb.accounting.NominalAccountNut;
import com.vlee.ejb.accounting.NominalAccountObject;
import com.vlee.ejb.accounting.NominalAccountTxn;
import com.vlee.ejb.accounting.NominalAccountTxnBean;
import com.vlee.ejb.accounting.NominalAccountTxnNut;
import com.vlee.ejb.accounting.NominalAccountTxnObject;
import com.vlee.ejb.accounting.OfficialReceipt;
import com.vlee.ejb.accounting.OfficialReceiptNut;
import com.vlee.ejb.accounting.OfficialReceiptObject;
import com.vlee.ejb.customer.CustAccount;
import com.vlee.ejb.customer.CustAccountBean;
import com.vlee.ejb.customer.CustAccountNut;
import com.vlee.ejb.customer.CustServiceCenterNut;
import com.vlee.ejb.customer.CustServiceCenterObject;
import com.vlee.ejb.customer.Invoice;
import com.vlee.ejb.customer.InvoiceBean;
import com.vlee.ejb.customer.InvoiceItem;
import com.vlee.ejb.customer.InvoiceItemNut;
import com.vlee.ejb.customer.InvoiceItemObject;
import com.vlee.ejb.customer.InvoiceNut;
import com.vlee.ejb.customer.InvoiceObject;
import com.vlee.ejb.customer.POSItem;
import com.vlee.ejb.customer.POSItemBean;
import com.vlee.ejb.customer.POSItemChildObject;
import com.vlee.ejb.customer.POSItemNut;
import com.vlee.ejb.customer.POSItemObject;
import com.vlee.ejb.inventory.Item;
import com.vlee.ejb.inventory.ItemBean;
import com.vlee.ejb.inventory.ItemNut;
import com.vlee.ejb.inventory.ItemObject;
import com.vlee.ejb.inventory.Location;
import com.vlee.ejb.inventory.LocationNut;
import com.vlee.ejb.inventory.Stock;
import com.vlee.ejb.inventory.StockNut;
import com.vlee.ejb.inventory.StockObject;
import com.vlee.ejb.user.RoleBean;
import com.vlee.ejb.user.User;
import com.vlee.ejb.user.UserDetails;
import com.vlee.ejb.user.UserDetailsNut;
import com.vlee.ejb.user.UserNut;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionDo;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.CurrencyFormat;
import com.vlee.util.Log;
import com.vlee.util.Task;
import com.vlee.util.TimeFormat;

public class DoMigrateTopConDB implements Action
{
	private String strClassName = "DoMigrateTopConDB";
	private static Task curTask = null;
	// Constants
	private static Integer iDefLocId = new Integer(1000);
	private static Integer iDefSvcCtrId = new Integer(1);
	private static Integer iDefPCCenterId = new Integer(1000);
	private static Integer iDefCondId = new Integer(StockNut.STK_COND_GOOD);
	private static Integer iDefCashAccId = new Integer(1000);
	private static Integer iDefChequeAccId = new Integer(1001);
	private BranchObject defBranchObj = null;
	private static HashMap hmCurr = new HashMap();
	static
	{
		hmCurr.put(new Integer(1), "MYR");
		hmCurr.put(new Integer(2), "USD");
		hmCurr.put(new Integer(3), "SGD");
		hmCurr.put(new Integer(4), "4");
		hmCurr.put(new Integer(5), "5");
		hmCurr.put(new Integer(6), "6");
		hmCurr.put(new Integer(7), "7");
	}

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
			/*******************************************************************
			 * // 1. Extract BranchInfo
			 ******************************************************************/
//			String query = "select * from branchinfo";
//			Statement stmt = con.createStatement();
//			ResultSet rs = stmt.executeQuery(query);
//			curTask = new Task("Extract BranchInfo", rs.getFetchSize());
//			while (rs.next())
//			{
//				curTask.increment();
//				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
//				Log.printVerbose("Got one ... ");
//				String coNo = rs.getString("cono");
//				String branchName = rs.getString("branchname");
//				String coname = rs.getString("coname");
//				Log.printVerbose("coNo = " + coNo);
//				Log.printVerbose("branchName = " + branchName);
//				Log.printVerbose("coname = " + coname);
//				// insert to location, cust_svc_center, supp_svc_center
//				// Assumes locaddr has a default row of pkid = 0
//				// NEW: Discovered that they are 2 Locid ("00","99")
//				Location newLoc = LocationNut.getObjectByCode("00");
//				if (newLoc == null)
//				{
//					newLoc = LocationNut.getHome().create(LocationBean.NS_TRADING, "", "00", branchName + " (00)",
//							coname, iDefPCCenterId, new Integer(0), TimeFormat.getTimestamp(), usrid);
//				}
//				Location newLoc2 = LocationNut.getObjectByCode("99");
//				if (newLoc2 == null)
//				{
//					newLoc2 = LocationNut.getHome().create(LocationBean.NS_TRADING, "", "99", branchName + " (99)",
//							coname, iDefPCCenterId, new Integer(0), TimeFormat.getTimestamp(), usrid);
//				}
//				Branch newBranch = BranchNut.getObjectByCode(coNo);
//				if (newBranch == null)
//				{
//					BranchObject newBranchObj = new BranchObject();
//					newBranchObj.code = coNo;
//					// newBranchObj.regNo = "";
//					newBranchObj.name = branchName;
//					newBranchObj.description = coname;
//					// newBranchObj.addr1 = req.getParameter("addr1");
//					// newBranchObj.addr2 = req.getParameter("addr2");
//					// newBranchObj.addr3 = req.getParameter("addr3");
//					// newBranchObj.zip = req.getParameter("zip");
//					// newBranchObj.state = req.getParameter("state");
//					// newBranchObj.countryCode =
//					// req.getParameter("countryCode");
//					// newBranchObj.phoneNo = req.getParameter("phoneNo");
//					// newBranchObj.faxNo = req.getParameter("faxNo");
//					// newBranchObj.webUrl = req.getParameter("webUrl");
//					newBranchObj.accPCCenterId = iDefPCCenterId;
//					newBranchObj.invLocationId = iDefLocId;
//					// newBranchObj.cashbookCash = new
//					// Integer(req.getParameter("cashbookCash"));
//					// newBranchObj.cashbookCard = new
//					// Integer(req.getParameter("cashbookCard"));
//					// newBranchObj.cashbookCheque = new
//					// Integer(req.getParameter("cashbookCheque"));
//					// newBranchObj.cashbookPDCheque = new
//					// Integer(req.getParameter("cashbookPDCheque"));
//					// newBranchObj.currency = req.getParameter("currency");
//					// newBranchObj.pricing = req.getParameter("pricing");
//					// newBranchObj.hotlines = req.getParameter("hotlines");
//					// newBranchObj.logoURL= req.getParameter("logoURL");
//					newBranch = BranchNut.fnCreate(newBranchObj);
//				}
//			}
//			stmt.close();
			/*******************************************************************
			 * // 2. Extract Customer Info
			 ******************************************************************/
//			String custQuery = "select * from cus";
//			Statement custStmt = con.createStatement();
//			ResultSet rsCust = custStmt.executeQuery(custQuery);
//			// String cleanCust = "delete from cust_account_index where pkid !=
//			// 1";
//			// Statement cleanStmt = jbossCon.createStatement();
//			curTask = new Task("Extract Customer", rsCust.getFetchSize());
//			while (rsCust.next())
//			{
//				curTask.increment();
//				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
//				Log.printVerbose("Got a customer ... ");
//				String custId = rsCust.getString("custid");
//				String shortName = rsCust.getString("shortname");
//				String name1 = rsCust.getString("name1");
//				String custType = rsCust.getString("custtype");
//				String contact = rsCust.getString("contact");
//				String addr1 = rsCust.getString("addr1");
//				String addr2 = rsCust.getString("addr2");
//				String addr3 = rsCust.getString("addr3");
//				String tel = rsCust.getString("tel");
//				String fax = rsCust.getString("fax");
//				// Create the custAccount
//				CustAccount newCustAcc = CustAccountNut.getObjectByCode(custId);
//				if (newCustAcc == null)
//				{
//					CustAccountObject custObj = new CustAccountObject();
//					custObj.name = shortName;
//					custObj.custAccountCode = custId;
//					custObj.description = name1;
//					custObj.nameFirst = contact;
//					// custObj.accType = CustAccountBean.ACCTYPE_NORMAL_ENUM;
//					custObj.mainAddress1 = addr1;
//					custObj.mainAddress2 = addr2;
//					custObj.mainAddress3 = addr3;
//					custObj.telephone1 = tel;
//					custObj.faxNo = fax;
//					newCustAcc = CustAccountNut.fnCreate(custObj);
//					if (newCustAcc == null)
//						throw new Exception("Failed to create CustAccount " + custId);
//				}
//			}
//			custStmt.close();
			/*******************************************************************
			 * // 2a. Extract SalesRep Info
			 ******************************************************************/
//			String salesRepQuery = "select * from srep";
//			Statement salesRepStmt = con.createStatement();
//			ResultSet rsSalesRep = salesRepStmt.executeQuery(salesRepQuery);
//			curTask = new Task("Extract SalesRep", rsSalesRep.getFetchSize());
//			while (rsSalesRep.next())
//			{
//				curTask.increment();
//				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
//				String srId = rsSalesRep.getString("srid");
//				String srDesc = rsSalesRep.getString("description");
//				createNewSRep(srId, srDesc);
//			} // end rsSalesRep
			/*******************************************************************
			 * // 3. Extract Stock Info
			 ******************************************************************/
//			// Create a DUMMY Non-Inventory Item to track all stocks that have
//			// missing or unaccounted stkId
//			Item nonInvItem = ItemNut.getObjectByCode("non-inv");
//			if (nonInvItem == null)
//			{
//				ItemObject itemObj = new ItemObject();
//				// populate the properties here!!
//				itemObj.code = "non-inv";
//				itemObj.name = "Non-Inventory";
//				itemObj.description = "General Non-Inventory";
//				itemObj.userIdUpdate = usrid;
//				itemObj.enumInvType = new Integer(ItemBean.INV_TYPE_NONSTK);
//				nonInvItem = ItemNut.fnCreate(itemObj);
//			}
//			// Create a corresponding NON_INV PosItem
//			POSItem nonInvPOSItem = POSItemNut.getPOSItem(nonInvItem.getPkid(), POSItemBean.TYPE_NINV);
//			if (nonInvPOSItem == null)
//			{
//				POSItemObject newNIPosItemObj = new POSItemObject();
//				newNIPosItemObj.itemFKId = nonInvItem.getPkid();
//				newNIPosItemObj.itemType = POSItemBean.TYPE_NINV;
//				newNIPosItemObj.currency = "MYR";
//				// newNIPosItemObj.unitPriceStd = new BigDecimal("0.00");
//				// newNIPosItemObj.unitPriceDiscounted = new BigDecimal("0.00");
//				// newNIPosItemObj.unitPriceMin = new BigDecimal("0.00");
//				// newNIPosItemObj.timeEffective = TimeFormat.getTimestamp();
//				newNIPosItemObj.status = POSItemBean.STATUS_ACTIVE;
//				// newNIPosItemObj.lastUpdate = TimeFormat.getTimestamp();
//				newNIPosItemObj.userIdUpdate = usrid;
//				// newNIPosItemObj.costOfItem = new BigDecimal("0.00");
//				nonInvPOSItem = POSItemNut.fnCreate(newNIPosItemObj);
//			}
//			String stkQuery = "select * from stkmaster";
//			Statement stkStmt = con.createStatement();
//			ResultSet rsStk = stkStmt.executeQuery(stkQuery);
//			// String cleanCust = "delete from cust_account_index where pkid !=
//			// 1";
//			// Statement cleanStmt = jbossCon.createStatement();
//			curTask = new Task("Extract Stock", rsStk.getFetchSize());
//			while (rsStk.next())
//			{
//				curTask.increment();
//				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
//				String stkId = rsStk.getString("stkid");
//				String shortName = rsStk.getString("shortname");
//				String desc1 = rsStk.getString("desc1");
//				String desc2 = rsStk.getString("desc2");
//				String desc3 = rsStk.getString("desc3");
//				BigDecimal aveUCost = rsStk.getBigDecimal("aveucost");
//				BigDecimal rplUCost = rsStk.getBigDecimal("rplucost");
//				BigDecimal curBalQty = rsStk.getBigDecimal("curbalqty");
//				String catid = rsStk.getString("catid");// / map to category1 of
//														// inv_item
//				String deptid = rsStk.getString("deptid");// / map to
//															// category2 of
//															// inv_item
//				String grpid = rsStk.getString("grpid");// / map to category3 of
//														// inv_item
//				// Add the item index if doesn't already exist
//				Item newItem = ItemNut.getObjectByCode(stkId);
//				if (newItem == null)
//				{
//					String uomQuery = "select distinct uom from cdhisd where stkid = '" + stkId + "'";
//					Statement uomStmt = con.createStatement();
//					ResultSet rsUOM = uomStmt.executeQuery(uomQuery);
//					Integer thisUOM = new Integer(ItemBean.UOM_NONE);
//					ItemObject itemObj = new ItemObject();
//					if (rsUOM.next())
//					{
//						String uom = rsUOM.getString("uom");
//						itemObj.uom = uom;
//						if (uom.equals("PCE") || uom.equals("PCS"))
//							thisUOM = new Integer(ItemBean.UOM_PCS);
//						else if (uom.equals("ROLL"))
//							thisUOM = new Integer(ItemBean.UOM_ROLL);
//						else if (uom.equals("SET") || uom.equals("SETS") || uom.equals("SSET"))
//							thisUOM = new Integer(ItemBean.UOM_SET);
//						else if (uom.equals("UNIT") || uom.equals("NOS"))
//							thisUOM = new Integer(ItemBean.UOM_UNIT);
//					}
//					// populate the properties here!!
//					itemObj.code = stkId;
//					itemObj.name = shortName;
//					itemObj.description = desc1 + desc2 + desc3;
//					itemObj.userIdUpdate = usrid;
//					itemObj.enumInvType = new Integer(ItemBean.INV_TYPE_INVENTORY);
//					itemObj.categoryId = new Integer(1000);
//					itemObj.unitOfMeasure = thisUOM;
//					itemObj.category1 = catid;
//					itemObj.category2 = deptid;
//					itemObj.category3 = grpid;
//					newItem = ItemNut.fnCreate(itemObj);
//				}
//				// Then populate the POSITems
//				POSItem newPOSItem = POSItemNut.getPOSItem(newItem.getPkid(), POSItemBean.TYPE_INV);
//				if (newPOSItem == null)
//				{
//					POSItemObject newPOSItemObj = new POSItemObject();
//					newPOSItemObj.itemFKId = newItem.getPkid();
//					newPOSItemObj.itemType = POSItemBean.TYPE_INV;
//					newPOSItemObj.currency = "MYR";
//					newPOSItemObj.unitPriceStd = rplUCost;
//					// newPOSItemObj.unitPriceDiscounted = new
//					// BigDecimal("0.00");
//					// newPOSItemObj.unitPriceMin = new BigDecimal("0.00");
//					// newPOSItemObj.timeEffective = TimeFormat.getTimestamp();
//					newPOSItemObj.status = POSItemBean.STATUS_ACTIVE;
//					// newPOSItemObj.lastUpdate = TimeFormat.getTimestamp();
//					newPOSItemObj.userIdUpdate = usrid;
//					// newPOSItemObj.costOfItem = new BigDecimal("0.00");
//					newPOSItem = POSItemNut.fnCreate(newPOSItemObj);
//				}
//				// Then populate the corresponding stock table (only 1 row for
//				// each item since we only have one warehouse
//				Stock newStk = StockNut.getObjectBy(newItem.getPkid(), iDefLocId, iDefCondId, "");
//				if (newStk == null)
//				{
//					StockObject newObj = new StockObject();
//					newObj.itemId = newItem.getPkid();
//					newObj.locationId = iDefLocId;
//					newObj.accPCCenterId = iDefPCCenterId;
//					newObj.userIdUpdate = usrid;
//					newStk = StockNut.fnCreate(newObj);
//				}
//			}
//			stkStmt.close();
			/*******************************************************************
			 * // 4. Extract Sales Txn
			 ******************************************************************/
			/*
			 * a. Sort the CDHis by TxDate b. For each row, switch(txType) if
			 * (I) // for invoice Create a SalesTxn from the top down to Nominal
			 * Account, in this manner: SalesOrder -> SOItems -> DO -> DOItems ->
			 * Invoice -> InvoiceItems -> Nominal Account
			 * 
			 * The soItems/invoiceItems taken from CDHisD table (foreign key =
			 * DocRef)
			 * 
			 * if (P) // for Payment Find each invoice mapped to the payment
			 * (foreign key = docRef) If > 1 mapping, i.e. payment is used to
			 * pay > 1 invoice, put that info in the remarks of the receipt for
			 * the time being, If 1 mapping, put that into SalesTxnId (TO_DO:
			 * Waiting for the acc_settlement table to be out) Update the
			 * NominalAccount accordingly (should be done inside Receipt)
			 * 
			 * if (C) // Credit Note Create new Credit Note (using Generic Stmt)
			 * 
			 * if (D) // Debit Note Create new Debit Note (using Generic Stmt) //
			 * may not be ready yet, but leave this out first
			 * 
			 * if (R) // Reverse Payment, used to bring the Invoice back to its
			 * original state, // e.g. "Acct Closed", "Credit Card Refund
			 * Reversed or", "Nsf", "Payment Reversal", "Resubmit Check",
			 * "Uncollectable", "Wrong Amount", "Wrong Customer", "Wrong
			 * Invoice" // Reverse Payment debits the Customer's Account which
			 * means, nullifies earlier payments.
			 * 
			 */
			Integer offset = null;
			Integer limit = null;
			if (req.getParameter("all") != null)
			{
				offset = new Integer(0);
			} else
			{
				try
				{
					offset = new Integer(req.getParameter("offset"));
				} catch (Exception ex)
				{
					offset = new Integer(0);
				}
				try
				{
					limit = new Integer(req.getParameter("limit"));
				} catch (Exception ex)
				{
				}
				;
			}
			// Integer limit = new Integer(100);
			// Read the CDHis table ...
			// String custTxnQuery = "select * from cdhis order by txdate";
			String custTxnQuery = "select * from cdhis ";
			// custTxnQuery += " where txstatus != 'D'"; // Ignore deleted docs
			// custTxnQuery += " where custid = 'SC0001' and (txtype = 'P' or
			// txtype = 'C')"; // Don't take Loan Account
			// custTxnQuery += " where custid in (select distinct custid from
			// cdhis where currid != 1) ";
			custTxnQuery += " where custid != 'AA0001'"; // Don't take Loan
															// Account
			custTxnQuery += " and txstatus != 'H'"; // Ignore invoices on hold
			// custTxnQuery += " where txtype = 'P'"; // Don't take Loan Account
			custTxnQuery += " order by txdate ";
			if (offset != null)
				custTxnQuery += " offset " + offset.intValue();
			if (limit != null)
				custTxnQuery += " limit " + limit.intValue();
			
			Log.printVerbose(custTxnQuery);
			// String custTxnQuery = "select * from cdhis where txtype = 'I'
			// order by txdate offset 10000 limit 100";
			// String custTxnQuery = "select * from cdhis where txtype = 'I'
			// order by txdate limit 10000";
			Statement custTxnStmt = con.createStatement();
			ResultSet rsCustTxn = custTxnStmt.executeQuery(custTxnQuery);
			/*
			 * // Clear the existing tables String cleanCashAccTxn = "delete
			 * from acc_cash_transactions"; String cleanNomAccTxn = "delete from
			 * acc_nominal_account_txn"; String cleanNomAcc = "delete from
			 * acc_nominal_account"; String cleanDocLink = "delete from
			 * acc_doclink"; String cleanGenStmt = "delete from
			 * acc_generic_stmt"; String cleanReceipt = "delete from
			 * cust_receipt_index"; String cleanInvoiceItem = "delete from
			 * cust_invoice_item"; String cleanDOItem = "delete from
			 * cust_delivery_order_item"; String cleanJobsheetItem = "delete
			 * from cust_jobsheet_item"; String cleanInvoice = "delete from
			 * cust_invoice_index"; String cleanDO = "delete from
			 * cust_delivery_order_index"; String cleanJobsheet = "delete from
			 * cust_jobsheet_index"; String cleanSalesTxn = "delete from
			 * cust_sales_txn_index"; String cleanTableCounter = "delete from
			 * app_table_counter" + " where tablename ~ 'jobsheet'" + " or
			 * tablename ~ 'delivery_order'" + " or tablename ~ 'acc_doclink'" + "
			 * or tablename ~ 'invoice'"; Statement cleanStmt =
			 * jbossCon.createStatement();
			 * cleanStmt.executeUpdate(cleanCashAccTxn);
			 * cleanStmt.executeUpdate(cleanNomAccTxn);
			 * cleanStmt.executeUpdate(cleanNomAcc);
			 * cleanStmt.executeUpdate(cleanDocLink);
			 * cleanStmt.executeUpdate(cleanGenStmt);
			 * cleanStmt.executeUpdate(cleanReceipt);
			 * cleanStmt.executeUpdate(cleanInvoiceItem);
			 * cleanStmt.executeUpdate(cleanDOItem);
			 * cleanStmt.executeUpdate(cleanJobsheetItem);
			 * cleanStmt.executeUpdate(cleanInvoice);
			 * cleanStmt.executeUpdate(cleanDO);
			 * cleanStmt.executeUpdate(cleanJobsheet);
			 * cleanStmt.executeUpdate(cleanSalesTxn);
			 * cleanStmt.executeUpdate(cleanTableCounter); cleanStmt.close();
			 */
			// int count = 0;
			curTask = new Task("Extract SalesTxns", rsCustTxn.getFetchSize());
			int count = offset.intValue();
			while (rsCustTxn.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				// Log.printVerbose("Processing Txn " + count);
				Log.printDebug("Processing Txn " + count);
				count++;
				// Get the txType
				String txType = rsCustTxn.getString("txtype");
				String docNo = rsCustTxn.getString("docno");
				String docRef = rsCustTxn.getString("docRef");
				String docInfo = rsCustTxn.getString("docInfo");
				String salesRepId = rsCustTxn.getString("srid");
				String custId = rsCustTxn.getString("custid");
				String currId = rsCustTxn.getString("currid");
				BigDecimal exchRate = rsCustTxn.getBigDecimal("exch_rate");
				Timestamp txDate = rsCustTxn.getTimestamp("txdate");
				BigDecimal term = rsCustTxn.getBigDecimal("term");
				BigDecimal txAmt = rsCustTxn.getBigDecimal("txamt");
				BigDecimal lTxAmt = rsCustTxn.getBigDecimal("ltxamt");
				// BigDecimal bfAmt = rsCustTxn.getBigDecimal("bfamt");
				// BigDecimal lBfAmt = rsCustTxn.getBigDecimal("lbfamt");
				BigDecimal debitAmt = rsCustTxn.getBigDecimal("debitamt");
				BigDecimal lDebitAmt = rsCustTxn.getBigDecimal("ldebitamt");
				BigDecimal creditAmt = rsCustTxn.getBigDecimal("creditamt");
				BigDecimal lCreditAmt = rsCustTxn.getBigDecimal("lcreditamt");
				String postDate = rsCustTxn.getString("postdate");
				String comment = rsCustTxn.getString("comment");
				String txStatus = rsCustTxn.getString("txstatus");
				String batchNo = rsCustTxn.getString("batchno");
				String itemNo = rsCustTxn.getString("itemno");
				BigDecimal netSales = rsCustTxn.getBigDecimal("netsales");
				BigDecimal lNetSales = rsCustTxn.getBigDecimal("lnetsales");
				BigDecimal lTotalCost = rsCustTxn.getBigDecimal("ltotalcost");
				String ageingDate = rsCustTxn.getString("ageingdate");
				String lIntDate = rsCustTxn.getString("lintdate");
				// Check if Customer exist
				CustAccount thisCustAcc = CustAccountNut.getObjectByCode(custId);
				if (thisCustAcc == null)
				{
					Log.printDebug(count + ": Cust Code = " + custId
							+ " doesn't exist in StkMaster, creating a temp one!!");
					// continue;
					thisCustAcc = CustAccountNut.getHome().create(custId, custId, custId,
							CustAccountBean.ACCTYPE_CORPORATE, TimeFormat.getTimestamp(), usrid);
				}
				// Check if SalesRep exist, else create new
				Integer thisSalesRepId = UserNut.getUserId(salesRepId);
				if (thisSalesRepId == null)
				{
					Log.printDebug(count + ": Sales Rep = " + salesRepId + " doesn't exist!!. Creating new ... ");
					thisSalesRepId = createNewSRep(salesRepId, salesRepId);
					// continue;
				}
				// Add docNo into the remarks
				String remarks = comment + "(Old DocRef = " + docNo + ")";
				String thisCurr = (String) hmCurr.get(new Integer(currId));
				if (!thisCurr.equals("MYR"))
				{
					remarks += ", (CURR=" + thisCurr + ")";
				}
				// Derive the LAmt instead of trusting the lTxAmt
				/*
				 * BigDecimal derivedLAmt =
				 * txAmt.divide(exchRate,BigDecimal.ROUND_HALF_UP);
				 */
				BigDecimal derivedLAmt = lDebitAmt;
				if (txType.equals("I"))
				{
					// This is an Invoice
					Log.printDebug(count + ": Processing Invoice ... ");
					if (txStatus.equals("D"))
					{
						Log.printDebug("STATUS = D, Skipping this entry ... ");
						continue;
					}
					/*
					 * DEPRECATED // Create Sales Txn Log.printVerbose(count + ":
					 * Creating SalesTxn ... "); SalesTxn newSalesTxn =
					 * SalesTxnNut.getHome().create(thisCustAcc.getPkid(),
					 * iDefSvcCtrId,txDate,remarks,SalesTxnBean.ST_CREATED,
					 * "",new Integer(0),TimeFormat.getTimestamp(),usrid);
					 * if(txStatus.equals("D"))
					 * newSalesTxn.setStatus(SalesTxnBean.STATUS_CANCELLED);
					 */
					/*
					 * SUPPRESS CREATION OF SALESORDER AND DO // Automatically
					 * create a SalesOrder Log.printVerbose(count + ": Creating
					 * SalesOrder ... "); Jobsheet newSO =
					 * //JobsheetNut.getHome().create(newSalesTxn.getPkid(),
					 * JobsheetNut.getHome().create(new Long(0),
					 * thisCurr,thisSalesRepId,thisSalesRepId,remarks,
					 * JobsheetBean.TYPE_SO,JobsheetBean.STATE_DO_OK,
					 * TimeFormat.getTimestamp(),TimeFormat.getTimestamp(),
					 * usrid); if(txStatus.equals("D"))
					 * newSO.setStatus(JobsheetBean.STATUS_CANCELLED);
					 *  // Query for Invoice Details from CDHisD String
					 * txnDQuery = "select * from cdhisd where docref = '" +
					 * docNo + "'"; Statement txnDStmt = con.createStatement();
					 * ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					 *  // Create DO if row > 0 DeliveryOrder newDO = null; if
					 * (rsTxnD.getFetchSize() > 0) { newDO =
					 * DeliveryOrderNut.getHome().create( //
					 * newSalesTxn.getPkid(), // deprecated new Long(0),
					 * thisSalesRepId,thisSalesRepId,remarks,
					 * DeliveryOrderBean.STATE_CREATED,
					 * txDate,TimeFormat.getTimestamp(),usrid);
					 * if(txStatus.equals("D"))
					 * newDO.setStatus(DeliveryOrderBean.STATUS_CANCELLED);
					 *  // Advance the SO state to DO_OK
					 * newSO.setState(JobsheetBean.STATE_DO_OK); }
					 */
					// Query for Invoice Details from CDHisD
					String txnDQuery = "select * from cdhisd where docref = '" + docNo + "'";
					Statement txnDStmt = con.createStatement();
					ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					// Automatically create an Invoice
					Log.printVerbose(count + ": Creating Invoice ... ");
					// BigDecimal derivedLAmt =
					// txAmt.divide(exchRate,BigDecimal.ROUND_HALF_UP);
					if (derivedLAmt.signum() == 0)
					{
						// Use the lDebitAmt instead
						derivedLAmt = lDebitAmt;
					}
					Invoice newInvoice = null;
					InvoiceObject newInvObj = new InvoiceObject();
					/** Populate the InvoiceObject * */
					// newInvObj.mPkid = new Long("0");
					// newInvObj.mStmtNumber= new Long("0");
					// newInvObj.mSalesTxnId = // deprecated
					// newInvObj.mPaymentTermsId = InvoiceBean.PAYMENT_CASH;
					newInvObj.mTimeIssued = txDate;
					// newInvObj.mCurrency = thisCurr;
					newInvObj.mCurrency = "MYR";
					newInvObj.mTotalAmt = derivedLAmt;
					newInvObj.mOutstandingAmt = derivedLAmt;
					newInvObj.mRemarks = remarks;
					newInvObj.mState = InvoiceBean.ST_POSTED;
					// newInvObj.mStatus = InvoiceBean.STATUS_ACTIVE;
					// newInvObj.mLastUpdate = TimeFormat.getTimestamp();
					newInvObj.mUserIdUpdate = usrid;
					newInvObj.mEntityTable = CustAccountBean.TABLENAME;
					newInvObj.mEntityKey = thisCustAcc.getPkid();
					newInvObj.mEntityName = thisCustAcc.getName(); // ???
					// newInvObj.mEntityType = ""; // ???
					// newInvObj.mIdentityNumber = "";
					newInvObj.mEntityContactPerson = newInvObj.mEntityName;
					// newInvObj.mForeignTable = "";
					// newInvObj.mForeignKey = new Integer(0);
					// newInvObj.mForeignText = "";
					// In order to derive the locationId and pccenter,
					// need to get the custsvcObject
					newInvObj.mCustSvcCtrId = iDefSvcCtrId;
					CustServiceCenterObject thisCSCObj = CustServiceCenterNut.getObject(newInvObj.mCustSvcCtrId);
					newInvObj.mLocationId = thisCSCObj.invLocationId;
					newInvObj.mPCCenter = thisCSCObj.accPCCenterId;
					// newInvObj.mTxnType = "";
					// newInvObj.mStmtType = "";
					// newInvObj.mReferenceNo = "";
					// newInvObj.mDescription = "";
					// newInvObj.mWorkOrder = new Long(0);
					// newInvObj.mDeliveryOrder = new Long(0);
					// newInvObj.mReceiptId = new Long(0);
					// newInvObj.mDisplayFormat = "inv1";
					// newInvObj.mDocType = "inv";
					newInvoice = InvoiceNut.fnCreate(newInvObj);
					// / ADDED BY VINCENT LEE - 2005-06-15
					{
						NominalAccountObject naObj = NominalAccountNut.getObject(CustAccountBean.TABLENAME,
								newInvObj.mPCCenter, newInvObj.mEntityKey, newInvObj.mCurrency);
						if (naObj == null)
						{
							naObj = new NominalAccountObject();
							// code = "not_used";
							naObj.namespace = NominalAccountBean.NS_CUSTOMER;
							naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
							naObj.foreignKey = newInvObj.mEntityKey;
							naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
							naObj.currency = newInvObj.mCurrency;
							naObj.amount = new BigDecimal(0);
							naObj.remarks = newInvObj.mRemarks;
							naObj.accPCCenterId = newInvObj.mPCCenter;
							naObj.userIdUpdate = newInvObj.mUserIdUpdate;
							NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
							naObj = naEJB.getObject();
						}
						NominalAccountTxnObject natObj = new NominalAccountTxnObject();
						natObj.nominalAccount = naObj.pkid;
						natObj.foreignTable = InvoiceBean.TABLENAME;
						natObj.foreignKey = newInvObj.mPkid;
						natObj.code = "not_used";
						natObj.info1 = " ";
						// natObj.description = " ";
						natObj.description = newInvObj.mRemarks;
						natObj.txnType = " ";
						natObj.glCodeDebit = GLCodeBean.ACC_RECEIVABLE;
						natObj.glCodeCredit = GLCodeBean.GENERAL_SALES;
						natObj.currency = newInvObj.mCurrency;
						natObj.amount = newInvObj.mTotalAmt;
						natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
						natObj.timeParam1 = newInvObj.mTimeIssued;
						natObj.timeOption2 = NominalAccountTxnBean.TIME_DUE;
						natObj.timeParam2 = newInvObj.mTimeIssued;
						natObj.state = NominalAccountTxnBean.ST_ACTUAL;
						natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
						natObj.lastUpdate = TimeFormat.getTimestamp();
						natObj.userIdUpdate = newInvObj.mUserIdUpdate;
						NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
					}
					/*
					 * if(txStatus.equals("D"))
					 * newInvoice.setStatus(InvoiceBean.STATUS_CANCELLED);
					 */
					// populate the exchrate
					// newInvoice.setXRate(exchRate);
					Long newInvoiceId = newInvoice.getPkid();
					// Create Items under the Invoice
					int itemCount = 0;
					while (rsTxnD.next())
					{
						itemCount++;
						Log.printVerbose(count + ": Creating SalesOrder Item " + itemCount);
						String itemDocRef = rsTxnD.getString("docRef");
						String itemItemNo = rsTxnD.getString("itemno");
						String itemTxDate = rsTxnD.getString("txdate");
						String itemGlId = rsTxnD.getString("glid");
						String itemStkId = rsTxnD.getString("stkId");
						String itemLocId = rsTxnD.getString("locId");
						BigDecimal itemQty = rsTxnD.getBigDecimal("qty");
						String itemUom = rsTxnD.getString("uom");
						BigDecimal itemUnitPrice = rsTxnD.getBigDecimal("unitprice");
						BigDecimal itemItemTotal = rsTxnD.getBigDecimal("itemtotal");
						BigDecimal itemLItemTotal = rsTxnD.getBigDecimal("litemtotal");
						BigDecimal itemNetSales = rsTxnD.getBigDecimal("netsales");
						BigDecimal itemLNetSales = rsTxnD.getBigDecimal("lnetsales");
						BigDecimal itemLTotalCost = rsTxnD.getBigDecimal("ltotalcost");
						String itemDescOrig = rsTxnD.getString("desc1");
						String itemDesc = itemDescOrig;
						// Add itemGlId into itemDesc
						// itemDesc = itemDesc + ", (GLID=" + itemGlId + ")";
						Integer thisPOSItemId = null;
						POSItem thisPOSItem = null;
						// Find the InvItem
						if (!itemStkId.trim().equals(""))
						{
							// Use the general non-inv code for the time being
							Item thisItem = ItemNut.getObjectByCode(itemStkId);
							if (thisItem == null)
							{
								Log.printDebug(count + ": Item Code " + itemStkId
										+ " doesn't exist!! Creating a temp one ...");
								// continue;
								/**
								 * *** BEGIN: CREATE THE ITEM BASED ON CDHISD
								 * ****
								 */
								ItemObject itemObj = new ItemObject();
								// populate the properties here!!
								itemObj.code = itemStkId;
								itemObj.name = itemDesc;
								itemObj.description = itemDesc;
								itemObj.userIdUpdate = usrid;
								itemObj.enumInvType = new Integer(ItemBean.INV_TYPE_INVENTORY);
								itemObj.uom = itemUom;
								/*
								 * itemObj.priceList = sellingPrice;
								 * itemObj.priceSale = sellingPrice;
								 * itemObj.priceDisc1 = sellingPrice;
								 * itemObj.priceDisc2 = sellingPrice;
								 * itemObj.priceDisc3 = sellingPrice;
								 * itemObj.priceMin = costPrice;
								 * itemObj.fifoUnitCost = costPrice;
								 * itemObj.maUnitCost = costPrice;
								 * itemObj.waUnitCost = costPrice;
								 * itemObj.lastUnitCost = costPrice;
								 * itemObj.replacementUnitCost = costPrice;
								 * itemObj.preferredSupplier = suppAccObj.pkid;
								 */
								thisItem = ItemNut.fnCreate(itemObj);
								POSItem auxPOSItem = POSItemNut.getPOSItem(thisItem.getPkid(), POSItemBean.TYPE_INV);
								if (auxPOSItem == null)
								{
									POSItemObject posObj = new POSItemObject();
									posObj.itemFKId = thisItem.getPkid();
									posObj.itemType = POSItemBean.TYPE_INV;
									posObj.currency = "MYR";
									posObj.unitPriceStd = itemUnitPrice;
									posObj.unitPriceDiscounted = itemUnitPrice;
									posObj.unitPriceMin = itemUnitPrice;
									posObj.userIdUpdate = usrid;
									auxPOSItem = POSItemNut.fnCreate(posObj);
								}
								// Then populate the corresponding stock table
								Integer thisLocId = iDefLocId;
								if (!itemLocId.equals(""))
								{
									Location thisLoc = LocationNut.getObjectByCode(itemLocId);
									if (thisLoc != null)
										thisLocId = thisLoc.getPkid();
								}
								Stock auxStk =
								// StockNut.getObjectBy(thisItem.getPkid(),iDefLocId,
								StockNut.getObjectBy(thisItem.getPkid(), thisLocId, iDefCondId, "");
								if (auxStk == null)
								{
									StockObject newObj = new StockObject();
									newObj.itemId = thisItem.getPkid();
									newObj.locationId = thisLocId;
									newObj.accPCCenterId = iDefPCCenterId;
									newObj.userIdUpdate = usrid;
									auxStk = StockNut.fnCreate(newObj);
								}
								/** *** END: CREATE THE ITEM BASED ON CDHISD **** */
							} // end if (thisitem == null)
							// Get the corresponding POSItem
							thisPOSItem = POSItemNut.getPOSItem(thisItem.getPkid(), POSItemBean.TYPE_INV);
							/*
							 * if (thisPOSItem == null) { Log.printDebug(count + ":
							 * POSItem with itemid " + thisItem.getPkid() + "
							 * doesn't exist!!"); continue; }
							 */
							if (thisPOSItem == null)
							{
								POSItemObject posObj = new POSItemObject();
								posObj.itemFKId = thisItem.getPkid();
								posObj.itemType = POSItemBean.TYPE_INV;
								posObj.currency = "MYR";
								posObj.unitPriceStd = itemUnitPrice;
								posObj.unitPriceDiscounted = itemUnitPrice;
								posObj.unitPriceMin = itemUnitPrice;
								posObj.userIdUpdate = usrid;
								thisPOSItem = POSItemNut.fnCreate(posObj);
							}
							thisPOSItemId = thisPOSItem.getPkid();
						} // end if (stkId != "")
						else
						{
							// Assume general non-inventory
							thisPOSItemId = POSItemBean.PKID_NONINV;
						}
						/*
						 * SUPPRESS CREATION OF JS and DO Items // Create
						 * JobsheetItem JobsheetItem newJSItem =
						 * JobsheetItemNut.getHome().create(
						 * newSO.getPkid(),thisPOSItemId,
						 * itemQty,thisCurr,itemDesc,itemUnitPrice,itemUnitPrice);
						 * if(txStatus.equals("D"))
						 * newJSItem.setStatus(JobsheetItemBean.STATUS_CANCELLED);
						 *  // Create DOItem DeliveryOrderItem newDOItem =
						 * DeliveryOrderItemNut.getHome().create(
						 * newDO.getPkid(),newJSItem.getPkid(),
						 * itemQty,itemDesc); if(txStatus.equals("D"))
						 * newDOItem.setStatus(DeliveryOrderItemBean.STATUS_CANCELLED);
						 */
						// Create InvoiceItem
						InvoiceItemObject invItemObj = new InvoiceItemObject();
						invItemObj.mInvoiceId = newInvoiceId;
						invItemObj.mPosItemId = thisPOSItemId;
						invItemObj.mTotalQty = itemQty;
						// invItemObj.mCurrency = thisCurr;
						invItemObj.mCurrency = "MYR";
						invItemObj.mRemarks = itemDesc;
						invItemObj.mUnitPriceQuoted = itemUnitPrice;
						if (thisPOSItemId.intValue() != POSItemBean.PKID_NONINV.intValue())
						{
							POSItemChildObject posChild = POSItemNut.getChildObject(thisPOSItemId);
							invItemObj.mPosItemType = posChild.itemType;
							invItemObj.mItemId = posChild.itemFKId;
							invItemObj.mItemCode = posChild.code;
							invItemObj.mName = posChild.name;
						} else
						{
							// for POSItemBean.PKID_NONINV
							Item nonInvItem2 = ItemNut.getObjectByCode("non-inv");
							invItemObj.mPosItemType = POSItemBean.TYPE_NINV;
							invItemObj.mItemId = nonInvItem2.getPkid();
							invItemObj.mItemCode = "non-inv";
							invItemObj.mName = nonInvItem2.getName();
						}
						InvoiceItem newInvoiceItem = InvoiceItemNut.fnCreate(invItemObj);
						/*
						 * if(txStatus.equals("D"))
						 * newInvoiceItem.setStatus(InvoiceItemBean.STATUS_CANCELLED);
						 */
					} // end while (rsTxnD.next())
					txnDStmt.close();
					// Do some reconciliation here,
					// basically, trust the txAmt in CDHis, and not the
					// InvoiceItems
					BigDecimal totalInvItemAmt = InvoiceNut.getInvoiceAmount(newInvoiceId);
					BigDecimal invAmt = InvoiceNut.getHandle(newInvoiceId).getTotalAmt();
					BigDecimal diff = invAmt.subtract(totalInvItemAmt);
					if (diff.signum() != 0)
					{
						// Add a non-inv item to the invoice balance it up
						// Create InvoiceItem
						String auxRemarks = "Difference between TxAmt and ItemTotal";
						InvoiceItemObject invItemObj = new InvoiceItemObject();
						invItemObj.mInvoiceId = newInvoiceId;
						invItemObj.mPosItemId = POSItemBean.PKID_NONINV;
						invItemObj.mTotalQty = new BigDecimal(1.00);
						// invItemObj.mCurrency = thisCurr;
						invItemObj.mCurrency = "MYR";
						invItemObj.mRemarks = auxRemarks;
						invItemObj.mUnitPriceQuoted = diff;
						// POSItemChildObject posChild =
						// POSItemNut.getChildObject(thisPOSItem.getPkid());
						invItemObj.mPosItemType = POSItemBean.TYPE_NINV;
						// invItemObj.mItemId = posChild.itemFKId;
						// invItemObj.mItemCode = posChild.code;
						// invItemObj.mName= posChild.name;
						InvoiceItem auxInvoiceItem = InvoiceItemNut.fnCreate(invItemObj);
						/*
						 * if(txStatus.equals("D"))
						 * auxInvoiceItem.setStatus(InvoiceItemBean.STATUS_CANCELLED);
						 */
					}
					/*
					 * // Post the Invoice to Nominal Account immediately //
					 * Alex: 06/23 - Post ONLY when tx is NOT DELETED // *******
					 * BEGIN POSTING *********** if (!txStatus.equals("D")) {
					 * NominalAccountObject naObj = NominalAccountNut.getObject(
					 * NominalAccountBean.FT_CUSTOMER, //iDefPCCenterId,
					 * thisCustAcc.getPkid(), thisCurr); iDefPCCenterId,
					 * thisCustAcc.getPkid(), "MYR"); // only one nominal
					 * account (MYR) even though transactions are in foreign
					 * currencies (info captured in invoice already) // Get the
					 * Invoice Amount // BigDecimal bdInvoiceAmt =
					 * InvoiceNut.getInvoiceAmount(newInvoiceId);
					 * 
					 * if(naObj==null) { naObj = new NominalAccountObject();
					 * //naObj.pkid = new Integer("0"); //naObj.code = new
					 * String("not_used"); naObj.namespace =
					 * NominalAccountBean.NS_CUSTOMER; naObj.foreignTable =
					 * NominalAccountBean.FT_CUSTOMER; naObj.foreignKey =
					 * thisCustAcc.getPkid(); naObj.accountType =
					 * NominalAccountBean.ACC_TYPE_RECEIVABLE; //naObj.currency =
					 * thisCurr; naObj.currency = "MYR"; // always use MYR for
					 * Topcon's case naObj.amount = new BigDecimal("0.00");
					 * //naObj.amount = naObj.amount.add(invAmt);
					 * //naObj.remarks = " "; naObj.remarks = remarks;
					 * naObj.accPCCenterId = iDefPCCenterId; naObj.state =
					 * NominalAccountBean.STATE_CREATED; naObj.status =
					 * NominalAccountBean.STATUS_ACTIVE; naObj.lastUpdate =
					 * TimeFormat.getTimestamp(); naObj.userIdUpdate = usrid;
					 * NominalAccount naEJB = NominalAccountNut.fnCreate(naObj); } //
					 * Gather necessary info to create the nominal txn
					 *  // Create the nominal account txn
					 * NominalAccountTxnObject natObj = new
					 * NominalAccountTxnObject(); natObj.nominalAccount =
					 * naObj.pkid; // Primary Key natObj.foreignTable =
					 * InvoiceBean.TABLENAME; natObj.foreignKey = newInvoiceId;
					 * natObj.code = "not_used"; natObj.info1 = " ";
					 * natObj.description = remarks; natObj.txnType =" ";
					 * natObj.glCodeDebit=NominalAccountBean.GLCODE_NOMINAL;
					 * natObj.glCodeCredit= GLCodeBean.GENERAL_SALES;
					 * natObj.currency = thisCurr; //natObj.currency = "MYR"; //
					 * always use MYR for Topcon's case natObj.amount = invAmt;
					 * natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
					 * natObj.timeParam1 = txDate; natObj.timeOption2 =
					 * NominalAccountTxnBean.TIME_DUE; natObj.timeParam2 =
					 * txDate; natObj.state = NominalAccountTxnBean.ST_ACTUAL;
					 * natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
					 * natObj.lastUpdate = TimeFormat.getTimestamp();
					 * natObj.userIdUpdate = usrid;
					 * 
					 * NominalAccountTxn natEJB =
					 * NominalAccountTxnNut.fnCreate(natObj); } // end if
					 * (txStatus != Deleted)
					 *  // ******* END POSTING ***********
					 */
				} // end if(txType = I) // INVOICE
				else if (txType.equals("P") && !txStatus.equals("D")) // for
																		// Payment
				{
					Log.printDebug("*** Detected a Receipt ***");
					// Query for Details from CDHisD
					String txnDQuery = "select * from cdhisd where docref = '" + docNo + "'";
					Statement txnDStmt = con.createStatement();
					ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					String strPayMethod = "cash"; // default to cash
					String strChequeNo = "";
					Integer iCashAccId = iDefCashAccId;
					// while(rsTxnD.next())
					if (rsTxnD.next())
					{
						// String glId = rsTxnD.getString("glid");
						String desc1 = rsTxnD.getString("desc1").trim();
						if (!desc1.equals("") && !desc1.startsWith("0/"))
						{
							strPayMethod = "cheque";
							strChequeNo = desc1;
							iCashAccId = iDefChequeAccId;
						}
						// remarks = remarks + ", (GLID=" + glId
						remarks = remarks + " (DESC=" + desc1 + ")";
					}
					txnDStmt.close();
					NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER,
					// iDefPCCenterId, thisCustAcc.getPkid(), thisCurr);
							iDefPCCenterId, thisCustAcc.getPkid(), "MYR");
					if (naObj == null)
					{
						naObj = new NominalAccountObject();
						// naObj.pkid = new Integer("0");
						// naObj.code = new String("not_used");
						naObj.namespace = NominalAccountBean.NS_CUSTOMER;
						naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
						naObj.foreignKey = thisCustAcc.getPkid();
						naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
						// naObj.currency = thisCurr;
						naObj.currency = "MYR";
						// naObj.amount = naObj.amount.add(lTxAmt);
						naObj.remarks = remarks;
						naObj.accPCCenterId = iDefPCCenterId;
						naObj.state = NominalAccountBean.STATE_CREATED;
						naObj.status = NominalAccountBean.STATUS_ACTIVE;
						naObj.lastUpdate = TimeFormat.getTimestamp();
						naObj.userIdUpdate = usrid;
						NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
					}
					/*
					 * // if derivedLAmt = 0, take the sum from payrefd if
					 * (derivedLAmt.signum() == 0) { derivedLAmt =
					 * getPaymentSum(con, docNo); Log.printVerbose(docNo + ":
					 * derivedLAmt = " + derivedLAmt); }
					 */
					// Alex (07/03) - must get from payrefd ALL THE TIME,
					// since we don't know if the payment if for multiple
					// customers
					// Vector vecPayment = getPaymentSum(con, docNo, thisCurr,
					// remarks, usrid);
					Vector vecPayment = getPaymentSum(con, docNo, "MYR", remarks, usrid);
					if (vecPayment.size() == 0)
					{
						Long newRcptId = createCustReceipt(naObj.pkid, iCashAccId, txDate, txDate, txDate,
						// thisCurr,lCreditAmt,strPayMethod,remarks,
								"MYR", lCreditAmt, strPayMethod, remarks, strChequeNo, // ""
																						// for
																						// Cash
								usrid, TimeFormat.getTimestamp());
					} else
					{
						for (int pymtCnt = 0; pymtCnt < vecPayment.size(); pymtCnt++)
						{
							Log.printVerbose("*** pymtCnt = " + pymtCnt + " ***");
							Payment thisPymt = (Payment) vecPayment.get(pymtCnt);
							// Create a Receipt
							Long newRcptId = createCustReceipt(thisPymt.nomAccId, iCashAccId, txDate, txDate, txDate,
							// thisCurr,thisPymt.amt,strPayMethod,remarks,
									"MYR", thisPymt.amt, strPayMethod, remarks, strChequeNo, // ""
																								// for
																								// Cash
									usrid, TimeFormat.getTimestamp());
						} // end for
					} // end if (vecPayment.size() == 0)
					/*
					 * // Find the invoices mapped to this receipt String
					 * doc2DocQ = "select * from payrefd where payno = '" +
					 * docNo + "'"; Statement doc2DocStmt =
					 * con.createStatement(); ResultSet rsDoc2Doc =
					 * doc2DocStmt.executeQuery(doc2DocQ);
					 * 
					 * String findInvQ = "select pkid from cust_invoice_index
					 * where remarks ~ ?"; PreparedStatement findInvStmt =
					 * jbossCon.prepareStatement(findInvQ);
					 * 
					 * while (rsDoc2Doc.next()) { String payNo =
					 * rsDoc2Doc.getString("payno"); String payType =
					 * rsDoc2Doc.getString("paytype"); String payInfo =
					 * rsDoc2Doc.getString("payinfo"); BigDecimal
					 * payRefDExchRate = rsDoc2Doc.getBigDecimal("exch_rate");
					 * String payRefDTxRefNo = rsDoc2Doc.getString("txrefno");
					 * String payRefDTxInfo = rsDoc2Doc.getString("txinfo");
					 * String payRefTxType = rsDoc2Doc.getString("txType");
					 * BigDecimal payRefDTxExchRate =
					 * rsDoc2Doc.getBigDecimal("txexch_rate"); Timestamp
					 * payRefDTxDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("txdate"), "MM/dd/yy HH:mm:ss");
					 * Timestamp payDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("paydate"), "MM/dd/yy HH:mm:ss");
					 * BigDecimal payAmt = rsDoc2Doc.getBigDecimal("payamt");
					 * BigDecimal lPayAmt = rsDoc2Doc.getBigDecimal("lpayamt");
					 * BigDecimal payRefDLTxAmt =
					 * rsDoc2Doc.getBigDecimal("ltxamt"); String posted =
					 * rsDoc2Doc.getString("posted"); String mthEnd =
					 * rsDoc2Doc.getString("mthend"); String payRefDCustId =
					 * rsDoc2Doc.getString("custId");
					 *  // Find the Invoice PKID corresponding to txRefNo
					 * findInvStmt.setString(1, payRefDTxRefNo); ResultSet
					 * rsFindInv = findInvStmt.executeQuery(); Long thisInvId =
					 * null; if(rsFindInv.next()) { thisInvId = new
					 * Long(rsFindInv.getLong("pkid"));
					 *  // Create the DocLink DocLink newDocLink =
					 * DocLinkNut.getHome().create( DocLinkBean.NS_CUSTOMER, "",
					 * DocLinkBean.RELTYPE_PAYMENT, ReceiptBean.TABLENAME,
					 * newRcptId, InvoiceBean.TABLENAME, thisInvId, thisCurr,
					 * lPayAmt, "", payAmt, "", TimeFormat.getTimestamp(),
					 * usrid); } else { //throw new Exception("Could not find
					 * Invoice " + payRefDTxRefNo); Log.printDebug("Could not
					 * find Invoice " + payRefDTxRefNo); } findInvStmt.close(); }
					 * doc2DocStmt.close();
					 */
				} // end if(txType = P) // PAYMENT
				else if (txType.equals("C") && !txStatus.equals("D")) // for
																		// CreditNote
				{
					Log.printDebug("*** Detected a CreditNote ***");
					// Query for Details from CDHisD
					String txnDQuery = "select * from cdhisd where docref = '" + docNo + "'";
					Statement txnDStmt = con.createStatement();
					ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					String creditNoteRemarks = remarks;
					while (rsTxnD.next())
					{
						String glId = rsTxnD.getString("glid");
						String stkId = rsTxnD.getString("stkid");
						String desc1 = rsTxnD.getString("desc1");
						if (!stkId.equals(""))
						{
							creditNoteRemarks = creditNoteRemarks + "," + stkId;
						} else
						{
							// remarks = remarks + ", (GLID=" + glId
							remarks = remarks + " (DESC=" + desc1 + ")";
							// creditNoteRemarks = creditNoteRemarks + ",
							// (GLID=" + glId
							creditNoteRemarks = creditNoteRemarks + " (DESC=" + desc1 + ")";
						}
						// Truncate remarks & creditNoteRemarks to 500 char
						if (remarks.length() > NominalAccountBean.MAX_LEN_REMARKS)
						{
							remarks = remarks.substring(0, NominalAccountBean.MAX_LEN_REMARKS);
						}
						if (creditNoteRemarks.length() > GenericStmtBean.MAX_LEN_REMARKS)
						{
							creditNoteRemarks = creditNoteRemarks.substring(0, GenericStmtBean.MAX_LEN_REMARKS);
						}
					}
					txnDStmt.close();
					NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER,
					// iDefPCCenterId, thisCustAcc.getPkid(), thisCurr);
							iDefPCCenterId, thisCustAcc.getPkid(), "MYR");
					if (naObj == null)
					{
						naObj = new NominalAccountObject();
						// naObj.pkid = new Integer("0");
						// naObj.code = new String("not_used");
						naObj.namespace = NominalAccountBean.NS_CUSTOMER;
						naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
						naObj.foreignKey = thisCustAcc.getPkid();
						naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
						// naObj.currency = thisCurr;
						naObj.currency = "MYR";
						// naObj.amount = naObj.amount.add(lTxAmt);
						naObj.remarks = remarks;
						naObj.accPCCenterId = iDefPCCenterId;
						naObj.state = NominalAccountBean.STATE_CREATED;
						naObj.status = NominalAccountBean.STATUS_ACTIVE;
						naObj.lastUpdate = TimeFormat.getTimestamp();
						naObj.userIdUpdate = usrid;
						NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
					}
					/*
					 * // if derivedLAmt = 0, take the sum from payrefd if
					 * (derivedLAmt.signum() == 0) { derivedLAmt =
					 * getPaymentSum(con, docNo); Log.printVerbose(docNo + ":
					 * derivedLAmt = " + derivedLAmt); }
					 *  // Create a Credit Note Long newCNId = createCreditNote(
					 * naObj.pkid, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
					 * GLCodeBean.CASH_DISCOUNT, txDate, InvoiceBean.TABLENAME,
					 * new Long(0), docNo, creditNoteRemarks,
					 * TimeFormat.getTimestamp(), usrid, derivedLAmt);
					 */
					// Alex (07/03) - must get from payrefd ALL THE TIME,
					// since we don't know if the payment if for multiple
					// customers
					// Vector vecPayment = getPaymentSum(con, docNo, thisCurr,
					// remarks,usrid);
					Vector vecPayment = getPaymentSum(con, docNo, "MYR", remarks, usrid);
					if (vecPayment.size() == 0)
					{
						Long newCNId = createCreditNote(naObj.pkid, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
								GLCodeBean.CASH_DISCOUNT, txDate, InvoiceBean.TABLENAME, new Long(0), docNo,
								creditNoteRemarks, TimeFormat.getTimestamp(),
								// usrid, thisCurr, lCreditAmt);
								usrid, "MYR", lCreditAmt);
					} else
					{
						for (int pymtCnt = 0; pymtCnt < vecPayment.size(); pymtCnt++)
						{
							Log.printVerbose("*** pymtCnt = " + pymtCnt + " ***");
							Payment thisPymt = (Payment) vecPayment.get(pymtCnt);
							// Create a Credit Note
							Long newCNId = createCreditNote(
							// naObj.pkid,
							// GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
									thisPymt.nomAccId, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
									GLCodeBean.CASH_DISCOUNT, txDate, InvoiceBean.TABLENAME, new Long(0), docNo,
									creditNoteRemarks, TimeFormat.getTimestamp(),
									// usrid, thisCurr, thisPymt.amt);
									usrid, "MYR", thisPymt.amt);
						} // end for
					} // end if (vecPayment.size() == 0)
					/*
					 * // Find the invoices mapped to this receipt String
					 * doc2DocQ = "select * from payrefd where payno = '" +
					 * docNo + "'"; Statement doc2DocStmt =
					 * con.createStatement(); ResultSet rsDoc2Doc =
					 * doc2DocStmt.executeQuery(doc2DocQ);
					 * 
					 * String findInvQ = "select pkid from cust_invoice_index
					 * where remarks ~ ?"; PreparedStatement findInvStmt =
					 * jbossCon.prepareStatement(findInvQ);
					 * 
					 * while (rsDoc2Doc.next()) { String payNo =
					 * rsDoc2Doc.getString("payno"); String payType =
					 * rsDoc2Doc.getString("paytype"); String payInfo =
					 * rsDoc2Doc.getString("payinfo"); BigDecimal
					 * payRefDExchRate = rsDoc2Doc.getBigDecimal("exch_rate");
					 * String payRefDTxRefNo = rsDoc2Doc.getString("txrefno");
					 * String payRefDTxInfo = rsDoc2Doc.getString("txinfo");
					 * String payRefTxType = rsDoc2Doc.getString("txType");
					 * BigDecimal payRefDTxExchRate =
					 * rsDoc2Doc.getBigDecimal("txexch_rate"); Timestamp
					 * payRefDTxDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("txdate"), "MM/dd/yy HH:mm:ss");
					 * Timestamp payDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("paydate"), "MM/dd/yy HH:mm:ss");
					 * BigDecimal payAmt = rsDoc2Doc.getBigDecimal("payamt");
					 * BigDecimal lPayAmt = rsDoc2Doc.getBigDecimal("lpayamt");
					 * BigDecimal payRefDLTxAmt =
					 * rsDoc2Doc.getBigDecimal("ltxamt"); String posted =
					 * rsDoc2Doc.getString("posted"); String mthEnd =
					 * rsDoc2Doc.getString("mthend"); String payRefDCustId =
					 * rsDoc2Doc.getString("custId");
					 *  // Find the Invoice PKID corresponding to txRefNo
					 * findInvStmt.setString(1, payRefDTxRefNo); ResultSet
					 * rsFindInv = findInvStmt.executeQuery(); Long thisInvId =
					 * null; if(rsFindInv.next()) { thisInvId = new
					 * Long(rsFindInv.getLong("pkid"));
					 *  // Create the DocLink DocLink newDocLink =
					 * DocLinkNut.getHome().create( DocLinkBean.NS_CUSTOMER, "",
					 * DocLinkBean.RELTYPE_CN, GenericStmtBean.TABLENAME,
					 * newCNId, InvoiceBean.TABLENAME, thisInvId, thisCurr,
					 * lPayAmt, "", payAmt, "", TimeFormat.getTimestamp(),
					 * usrid); } else { //throw new Exception("Could not find
					 * Invoice " + payRefDTxRefNo); Log.printDebug("Could not
					 * find Invoice " + payRefDTxRefNo); } findInvStmt.close(); }
					 * doc2DocStmt.close();
					 */
				} // end if(txType = C) // CreditNote
				else if (txType.equals("D") && !txStatus.equals("D")) // for
																		// DebitNote
				{
					Log.printDebug("*** Detected a DebitNote ***");
					// Query for Details from CDHisD
					String txnDQuery = "select * from cdhisd where docref = '" + docNo + "'";
					Statement txnDStmt = con.createStatement();
					ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					String debitNoteRemarks = remarks;
					while (rsTxnD.next())
					{
						String glId = rsTxnD.getString("glid");
						String stkId = rsTxnD.getString("stkid");
						String desc1 = rsTxnD.getString("desc1");
						if (!stkId.equals(""))
						{
							debitNoteRemarks = debitNoteRemarks + "," + stkId;
						} else
						{
							// remarks = remarks + ", (GLID=" + glId
							remarks = remarks + " (DESC=" + desc1 + ")";
							// debitNoteRemarks = debitNoteRemarks + ", (GLID="
							// + glId
							debitNoteRemarks = debitNoteRemarks + " (DESC=" + desc1 + ")";
						}
						// Truncate debitNoteRemarks to 500 char
						if (debitNoteRemarks.length() > GenericStmtBean.MAX_LEN_REMARKS)
						{
							debitNoteRemarks = debitNoteRemarks.substring(0, GenericStmtBean.MAX_LEN_REMARKS);
						}
					}
					txnDStmt.close();
					NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER,
					// iDefPCCenterId, thisCustAcc.getPkid(), thisCurr);
							iDefPCCenterId, thisCustAcc.getPkid(), "MYR");
					if (naObj == null)
					{
						naObj = new NominalAccountObject();
						// naObj.pkid = new Integer("0");
						// naObj.code = new String("not_used");
						naObj.namespace = NominalAccountBean.NS_CUSTOMER;
						naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
						naObj.foreignKey = thisCustAcc.getPkid();
						naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
						// naObj.currency = thisCurr;
						naObj.currency = "MYR";
						// naObj.amount = naObj.amount.add(lTxAmt);
						naObj.remarks = remarks;
						naObj.accPCCenterId = iDefPCCenterId;
						naObj.state = NominalAccountBean.STATE_CREATED;
						naObj.status = NominalAccountBean.STATUS_ACTIVE;
						naObj.lastUpdate = TimeFormat.getTimestamp();
						naObj.userIdUpdate = usrid;
						NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
					}
					// if derivedLAmt = 0, take the sum from payrefd
					if (derivedLAmt.signum() == 0)
					{
						// derivedLAmt = getRevPaymentSum(con, docNo);
						derivedLAmt = lDebitAmt;
						Log.printVerbose(docNo + ": derivedLAmt = " + derivedLAmt);
					}
					// Create a Debit Note
					Long newDNId = createDebitNote(naObj.pkid, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
							GLCodeBean.INTEREST_REVENUE, txDate, InvoiceBean.TABLENAME, new Long(0), docNo,
							debitNoteRemarks, TimeFormat.getTimestamp(),
							// usrid, thisCurr, derivedLAmt);
							usrid, "MYR", derivedLAmt);
					/*
					 * // Find the invoices mapped to this receipt String
					 * doc2DocQ = "select * from payrefd where payno = '" +
					 * docNo + "'"; Statement doc2DocStmt =
					 * con.createStatement(); ResultSet rsDoc2Doc =
					 * doc2DocStmt.executeQuery(doc2DocQ);
					 * 
					 * String findInvQ = "select pkid from cust_invoice_index
					 * where remarks ~ ?"; PreparedStatement findInvStmt =
					 * jbossCon.prepareStatement(findInvQ);
					 * 
					 * while (rsDoc2Doc.next()) { String payNo =
					 * rsDoc2Doc.getString("payno"); String payType =
					 * rsDoc2Doc.getString("paytype"); String payInfo =
					 * rsDoc2Doc.getString("payinfo"); BigDecimal
					 * payRefDExchRate = rsDoc2Doc.getBigDecimal("exch_rate");
					 * String payRefDTxRefNo = rsDoc2Doc.getString("txrefno");
					 * String payRefDTxInfo = rsDoc2Doc.getString("txinfo");
					 * String payRefTxType = rsDoc2Doc.getString("txType");
					 * BigDecimal payRefDTxExchRate =
					 * rsDoc2Doc.getBigDecimal("txexch_rate"); Timestamp
					 * payRefDTxDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("txdate"), "MM/dd/yy HH:mm:ss");
					 * Timestamp payDate = TimeFormat.createTimeStamp(
					 * rsDoc2Doc.getString("paydate"), "MM/dd/yy HH:mm:ss");
					 * BigDecimal payAmt = rsDoc2Doc.getBigDecimal("payamt");
					 * BigDecimal lPayAmt = rsDoc2Doc.getBigDecimal("lpayamt");
					 * BigDecimal payRefDLTxAmt =
					 * rsDoc2Doc.getBigDecimal("ltxamt"); String posted =
					 * rsDoc2Doc.getString("posted"); String mthEnd =
					 * rsDoc2Doc.getString("mthend"); String payRefDCustId =
					 * rsDoc2Doc.getString("custId");
					 *  // Find the Invoice PKID corresponding to txRefNo
					 * findInvStmt.setString(1, payRefDTxRefNo); ResultSet
					 * rsFindInv = findInvStmt.executeQuery(); Long thisInvId =
					 * null; if(rsFindInv.next()) { thisInvId = new
					 * Long(rsFindInv.getLong("pkid"));
					 *  // Create the DocLink DocLink newDocLink =
					 * DocLinkNut.getHome().create( DocLinkBean.NS_CUSTOMER, "",
					 * DocLinkBean.RELTYPE_DN, GenericStmtBean.TABLENAME,
					 * newDNId, InvoiceBean.TABLENAME, thisInvId, thisCurr,
					 * lPayAmt, "", payAmt, "", TimeFormat.getTimestamp(),
					 * usrid); } else { //throw new Exception("Could not find
					 * Invoice " + payRefDTxRefNo); Log.printDebug("Could not
					 * find Invoice " + payRefDTxRefNo); } findInvStmt.close(); }
					 * doc2DocStmt.close();
					 */
				} // end if(txType = D) // DebitNote
				else if (txType.equals("R") && !txStatus.equals("D")) // for
																		// ReversePayment
				{
					Log.printDebug("*** Detected a ReversePayment ***");
					// Query for Details from CDHisD
					String txnDQuery = "select * from cdhisd where docref = '" + docNo + "'";
					Statement txnDStmt = con.createStatement();
					ResultSet rsTxnD = txnDStmt.executeQuery(txnDQuery);
					while (rsTxnD.next())
					{
						String glId = rsTxnD.getString("glid");
						String desc1 = rsTxnD.getString("desc1");
						// remarks = remarks + ", (GLID=" + glId
						remarks = remarks + " (DESC=" + desc1 + ")";
					}
					txnDStmt.close();
					NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER,
					// iDefPCCenterId, thisCustAcc.getPkid(), thisCurr);
							iDefPCCenterId, thisCustAcc.getPkid(), "MYR");
					if (naObj == null)
					{
						naObj = new NominalAccountObject();
						// naObj.pkid = new Integer("0");
						// naObj.code = new String("not_used");
						naObj.namespace = NominalAccountBean.NS_CUSTOMER;
						naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
						naObj.foreignKey = thisCustAcc.getPkid();
						naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
						// naObj.currency = thisCurr;
						naObj.currency = "MYR";
						// naObj.amount = naObj.amount.add(lTxAmt);
						naObj.remarks = remarks;
						naObj.accPCCenterId = iDefPCCenterId;
						naObj.state = NominalAccountBean.STATE_CREATED;
						naObj.status = NominalAccountBean.STATUS_ACTIVE;
						naObj.lastUpdate = TimeFormat.getTimestamp();
						naObj.userIdUpdate = usrid;
						NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
					}
					/*
					 * // if derivedLAmt = 0, take the sum from payrefd if
					 * (derivedLAmt.signum() == 0) { //derivedLAmt =
					 * getRevPaymentSum(con, docNo); derivedLAmt = lDebitAmt;
					 * Log.printVerbose(docNo + ": derivedLAmt = " +
					 * derivedLAmt); }
					 */
					Vector vecRevPayment = getRevPaymentSum(con, docNo, "MYR", remarks, usrid);
					if (vecRevPayment.size() == 0)
					{
						// Create a ReversePayment
						Long newRPId = createReversePayment(GenericStmtBean.STMT_TYPE_REVERSE_PAYMENT, naObj.pkid,
								iDefCashAccId, "MYR", // thisCurr,
								derivedLAmt, "", remarks, "", txDate, "", new Long(0), usrid);
					} else
					{
						for (int pymtCnt = 0; pymtCnt < vecRevPayment.size(); pymtCnt++)
						{
							Log.printVerbose("*** pymtCnt = " + pymtCnt + " ***");
							Payment thisPymt = (Payment) vecRevPayment.get(pymtCnt);
							// Create a ReversePayment
							Long newRPId = createReversePayment(GenericStmtBean.STMT_TYPE_REVERSE_PAYMENT,
									thisPymt.nomAccId, // naObj.pkid,
									iDefCashAccId, "MYR", // thisCurr,
									thisPymt.amt, // derivedLAmt,
									"", remarks, "", txDate, "", new Long(0), usrid);
						} // end for
					} // end if (vecPayment.size() == 0)
					// Derive the receipt mapped to this reverse payment
					// Note: Only deriving for docno that is in the form
					// R/OR#####
					Log.printVerbose("*** reversePaymentRef = " + docNo + "***");
					/*
					 * if (docNo.startsWith("R/OR")) { String receiptRef =
					 * docNo.substring(2); Log.printVerbose("*** receiptRef = " +
					 * receiptRef + "***");
					 * 
					 * String findRcptQ = "select pkid from cust_receipt_index
					 * where payment_remarks ~ " + "'" + receiptRef + "'";
					 * Statement findRcptStmt = jbossCon.createStatement();
					 * ResultSet rsFindRcpt =
					 * findRcptStmt.executeQuery(findRcptQ);
					 * 
					 * if(rsFindRcpt.next()) { Long thisRcptId = new
					 * Long(rsFindRcpt.getLong("pkid"));
					 *  // Create the DocLink DocLink newDocLink =
					 * DocLinkNut.getHome().create( DocLinkBean.NS_CUSTOMER, "",
					 * DocLinkBean.RELTYPE_REV_PAYMENT,
					 * GenericStmtBean.TABLENAME, newRPId,
					 * ReceiptBean.TABLENAME, thisRcptId, thisCurr, lTxAmt, "",
					 * txAmt, "", TimeFormat.getTimestamp(), usrid); }
					 * findRcptStmt.close(); }
					 */
				} // end if(txType = R) // ReversePayment
				else
				{
					Log.printDebug("*** UNKNOWN txtype ***");
				}
			} // end while(rsCustTxn.next())
			custTxnStmt.close();
			/*******************************************************************
			 * FIXES AFTER POPULATION OF DOC INDICES
			 ******************************************************************/
			// 1. Fix all transactions that had gainloseamt != 0 owing to
			// differing exch_rates
			// fixExRate(con, jbossCon, usrid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while converting Topcon DB: " + ex.getMessage());
		}
		Log.printVerbose("***** END: MIGRATION OF TOPCON DB *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "CREATE DOC INDEX");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		// Clean up curTask;
		curTask = null;
		return new ActionRouter("test-migrate-topcondb-page");
	}

	private Integer createNewSRep(String srId, String srDesc) throws Exception
	{
		// Create the user
		Integer newUsrId = UserNut.getUserId(srId);
		User newUsr = null;
		if (newUsrId != null)
		{
			return newUsrId;
			// newUsr = UserNut.getHandle(newUsrId);
		} else
		{
			newUsr = UserNut.getHome().create(srId, srId, srDesc, "");
		}
		// Create the user details
		Collection colUsrDet = UserDetailsNut.getCollectionByUserId(newUsr.getUserId());
		if (colUsrDet == null || colUsrDet.isEmpty())
		{
			UserDetails thisUserD = UserDetailsNut.getHome().create(newUsr.getUserId(), Calendar.getInstance(), "", "",
					"", "", Calendar.getInstance());
		}
		// Assign a default role for the user
		Collection colUsrRole = ActionDo.getUserRoleHome().findUserRolesGiven("userid", newUsr.getUserId().toString());
		if (colUsrRole == null || colUsrRole.isEmpty())
		{
			ActionDo.getUserRoleHome().create(RoleBean.ROLEID_DEVELOPER, newUsr.getUserId(), Calendar.getInstance());
		}
		return newUsr.getUserId();
	}

	private Integer getUOM(String uom) throws Exception
	{
		Integer thisUOM = new Integer(ItemBean.UOM_NONE);
		if (uom.equals("PCE") || uom.equals("PCS"))
			thisUOM = new Integer(ItemBean.UOM_PCS);
		else if (uom.equals("ROLL"))
			thisUOM = new Integer(ItemBean.UOM_ROLL);
		else if (uom.equals("SET") || uom.equals("SETS") || uom.equals("SSET"))
			thisUOM = new Integer(ItemBean.UOM_SET);
		else if (uom.equals("UNIT") || uom.equals("NOS"))
			thisUOM = new Integer(ItemBean.UOM_UNIT);
		return thisUOM;
	}

	private Long createCustReceipt(Integer naPkid, Integer caPkid, Timestamp tsReceiptDate, Timestamp tsEffFromDate,
			Timestamp tsEffToDate, String currency, BigDecimal bdPaymentAmt, String strPaymentMethod,
			String strPaymentRmks,
			// String strGLCodeCredit,
			// String strGLCodeDebit,
			String strChequeNumber,
			// String strCashAccount,
			Integer iUsrId, Timestamp tsCreate) throws Exception
	{
		Long salesTxnId = new Long("0");
		String strAmountStr = CurrencyFormat.toWords(bdPaymentAmt, "", "");
		strAmountStr = strAmountStr + " ONLY";
		// / Get objects based on parameters above
		NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
		CashAccountObject caObj = CashAccountNut.getObject(caPkid);
		if (defBranchObj == null)
			defBranchObj = BranchNut.getObject(iDefSvcCtrId);
		/*
		 * /// 1) create a printable Cust Receipt Object ReceiptObject cRcptObj =
		 * new ReceiptObject(); cRcptObj.salesTxnId = salesTxnId;
		 * cRcptObj.custAccount = naObj.foreignKey; cRcptObj.intReserved1 = new
		 * Integer("0"); cRcptObj.strReserved1 = ""; //cRcptObj.currency =
		 * naObj.currency; cRcptObj.currency = currency; cRcptObj.amountStr =
		 * strAmountStr; cRcptObj.paymentAmount = bdPaymentAmt;
		 * cRcptObj.paymentMethod = strPaymentMethod; cRcptObj.paymentTime =
		 * tsReceiptDate; cRcptObj.chequeNumber = strChequeNumber;
		 * cRcptObj.bankId = caPkid; cRcptObj.paymentRemarks = strPaymentRmks;
		 * cRcptObj.glCode = GLCodeBean.GENERAL_SALES; cRcptObj.state =
		 * ReceiptBean.ST_CREATED; cRcptObj.status = ReceiptBean.STATUS_ACTIVE;
		 * cRcptObj.lastUpdate = tsCreate; cRcptObj.userIdUpdate = iUsrId;
		 * 
		 * Receipt sPayEJB = ReceiptNut.fnCreate(cRcptObj);
		 */
		// // create the receipt
		OfficialReceiptObject receiptObj = new OfficialReceiptObject();
		receiptObj.entityTable = CustAccountBean.TABLENAME;
		receiptObj.entityKey = naObj.foreignKey;
		receiptObj.entityName = CustAccountNut.getHandle(receiptObj.entityKey).getName();
		receiptObj.currency = currency;
		receiptObj.amount = bdPaymentAmt;
		receiptObj.paymentTime = tsReceiptDate;
		receiptObj.paymentMethod = strPaymentMethod;
		receiptObj.paymentRemarks = strPaymentRmks;
		receiptObj.chequeNumber = strChequeNumber;
		receiptObj.lastUpdate = TimeFormat.getTimestamp();
		receiptObj.userIdUpdate = iUsrId;
		receiptObj.cbCash = defBranchObj.cashbookCash;
		receiptObj.cbCard = defBranchObj.cashbookCard;
		receiptObj.cbCheque = defBranchObj.cashbookCheque;
		receiptObj.cbPDCheque = defBranchObj.cashbookPDCheque;
		if (strPaymentMethod.equals("cheque"))
		{
			receiptObj.amountCheque = bdPaymentAmt;
		} else
		{
			receiptObj.amountCash = bdPaymentAmt;
		}
		// receiptObj.amountCard = this.amountCard;
		// receiptObj.amountPDCheque = this.amountPDCheque;
		// if(this.amountPDCheque.signum()>0)
		// { receiptObj.chequeNumberPD = this.chequeNumberPD;}
		// receiptObj.datePDCheque = this.pdChequeDate;
		receiptObj.branch = defBranchObj.pkid;
		receiptObj.pcCenter = iDefPCCenterId;
		OfficialReceipt offRctEJB = OfficialReceiptNut.fnCreate(receiptObj);
		Log.printVerbose(receiptObj.toString());
		if (offRctEJB == null)
		{
			throw new Exception("Failed to create Receipt!!");
		}
		// / 2) create an entry in the nominal account transaction
		/*
		 * NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		 * natObj.nominalAccount = naObj.pkid; natObj.foreignTable =
		 * NominalAccountTxnBean.FT_CUST_RECEIPT; natObj.foreignKey =
		 * receiptObj.pkid; natObj.code = "not_used"; natObj.info1 = "";
		 * natObj.description = strPaymentRmks; natObj.glCodeCredit =
		 * GLCodeBean.ACC_RECEIVABLE; natObj.glCodeDebit = caObj.accountType;
		 * natObj.currency = naObj.currency; natObj.amount =
		 * bdPaymentAmt.negate(); natObj.timeOption1 =
		 * NominalAccountTxnBean.TIME_STMT; natObj.timeParam1 = tsReceiptDate;
		 * natObj.timeOption2 = NominalAccountTxnBean.TIME_NA; natObj.timeParam2 =
		 * tsReceiptDate; natObj.state = NominalAccountTxnBean.ST_CREATED;
		 * natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
		 * natObj.lastUpdate = tsCreate; natObj.userIdUpdate = iUsrId;
		 * NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
		 * 
		 * if(natEJB == null) { try { offRctEJB.remove();} catch(Exception ex) {
		 * ex.printStackTrace();} throw new Exception("Failed to create NAT"); }
		 * /// 4) update cash account transactions CashAccTxnObject catObj = new
		 * CashAccTxnObject(); catObj.txnCode = CashAccTxnBean.TXN_CODE_DEFAULT;
		 * catObj.glCodeCredit = GLCodeBean.ACC_RECEIVABLE; catObj.glCodeDebit =
		 * caObj.accountType; catObj.personInCharge = iUsrId; catObj.accFrom =
		 * new Integer("0");// another known bank account catObj.accTo =
		 * caObj.pkId; catObj.foreignTable = CashAccTxnBean.FT_CUST_RECEIPT;
		 * catObj.foreignKey = receiptObj.pkid; catObj.currency =
		 * naObj.currency; catObj.amount = bdPaymentAmt; catObj.txnTime =
		 * tsReceiptDate; catObj.remarks = strPaymentRmks; catObj.info1 = "";
		 * catObj.info2 = ""; catObj.state = CashAccTxnBean.ST_CREATED;
		 * catObj.status = CashAccTxnBean.STATUS_ACTIVE; catObj.lastUpdate =
		 * tsCreate; catObj.userIdUpdate = iUsrId; catObj.pcCenter =
		 * caObj.pcCenter; CashAccTxn catEJB = CashAccTxnNut.fnCreate(catObj);
		 * /// 5) update cash account balance // no need to set the balance !!!
		 * hurray !!!!
		 * 
		 * /// 3) update the nominal account NominalAccount naEJB =
		 * NominalAccountNut.getHandle(naObj.pkid); BigDecimal bdLatestBal =
		 * naEJB.getAmount(); bdLatestBal = bdLatestBal.add(natObj.amount);
		 * naObj.amount = bdLatestBal;
		 */
		return receiptObj.pkid;
	}

	private Long createCreditNote(Integer naPkid, String stmtType, String glCodeDebit, Timestamp stmtDate,
			String fStmtTable, Long fStmtKey, String refNo, String remarks, Timestamp timeUpdate, Integer userId,
			String currency, BigDecimal amount) throws Exception
	{
		// try
		// {
		String strErrMsg = null;
		/*
		 * Integer naPkid = new Integer(req.getParameter("naPkid")); String
		 * stmtType = req.getParameter("stmtType"); String glCodeDebit =
		 * req.getParameter("glCodeDebit"); Timestamp stmtDate =
		 * TimeFormat.createTimestamp( req.getParameter("stmtDate")); String
		 * fStmtTable = req.getParameter("fStmtTable"); Long fStmtKey = new
		 * Long(req.getParameter("fStmtKey")); String refNo =
		 * req.getParameter("refNo"); String remarks =
		 * req.getParameter("remarks"); Timestamp timeUpdate =
		 * TimeFormat.getTimestamp(); Integer userId =
		 * UserNut.getUserId(req.getParameter("userName")); BigDecimal amount =
		 * new BigDecimal(req.getParameter("amount"));
		 */
		// / checking input variables
		if (naPkid == null)
			throw new Exception(" Invalid Nominal Account PKID ");
		NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
		GenericStmtObject gsObj = new GenericStmtObject();
		gsObj.namespace = GenericStmtBean.NS_CUSTOMER;
		gsObj.dateStmt = stmtDate;
		// // assuming Credit Note here!!!!
		gsObj.glCodeDebit = glCodeDebit;
		gsObj.glCodeCredit = GLCodeBean.ACC_RECEIVABLE;
		// gsObj.currency = naObj.currency;
		gsObj.currency = currency; // don't inherit from NominalAccount
		gsObj.amount = amount;
		gsObj.dateDue = stmtDate;
		gsObj.foreignEntityTable = naObj.foreignTable;
		gsObj.foreignEntityKey = naObj.foreignKey;
		gsObj.pcCenter = naObj.accPCCenterId;
		gsObj.stmtType = stmtType;
		gsObj.foreignStmtTable = fStmtTable;
		gsObj.foreignStmtKey = fStmtKey;
		gsObj.referenceNo = refNo;
		// gsObj.remarks = remarks;
		gsObj.remarks = "AUTO-CN: " + remarks;
		gsObj.dateStart = gsObj.dateStmt;
		gsObj.dateEnd = gsObj.dateStmt;
		gsObj.dateDue = gsObj.dateStmt;
		gsObj.dateCreated = gsObj.dateStmt;
		gsObj.dateApproved = gsObj.dateStmt;
		gsObj.dateVerified = gsObj.dateStmt;
		gsObj.dateVerified = gsObj.dateStmt;
		gsObj.dateUpdate = timeUpdate;
		gsObj.userIdCreate = userId;
		gsObj.userIdPIC = userId;
		gsObj.userIdApprove = userId;
		gsObj.userIdVerified = userId;
		gsObj.userIdUpdate = userId;
		// create a nominal account txn for this generic stmt
		NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		natObj.nominalAccount = naObj.pkid;
		natObj.foreignTable = GenericStmtBean.TABLENAME;
		natObj.description = gsObj.remarks;
		natObj.glCodeDebit = gsObj.glCodeDebit;
		natObj.glCodeCredit = gsObj.glCodeCredit;
		natObj.currency = gsObj.currency;
		natObj.amount = gsObj.amount.negate();
		natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
		natObj.timeParam1 = gsObj.dateStmt;
		natObj.state = NominalAccountTxnBean.ST_ACTUAL;
		natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
		natObj.lastUpdate = TimeFormat.getTimestamp();
		natObj.userIdUpdate = gsObj.userIdCreate;
		GenericStmt gsEJB = GenericStmtNut.fnCreate(gsObj);
		if (gsEJB == null)
		{
			throw new Exception(" Unable to create Generic Stmt Bean ");
		}
		natObj.foreignKey = gsObj.pkid;
		NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
		if (natEJB == null)
		{
			try
			{
				gsEJB.remove();
			} catch (Exception ex)
			{
				ex.printStackTrace();
				throw new Exception(" Unable to create Nominal Account Txn ");
			}
		}
		naObj.amount = naObj.amount.add(natObj.amount);
		return gsObj.pkid;
	}

	private Long createDebitNote(Integer naPkid, String stmtType, String glCodeCredit, Timestamp stmtDate,
			String fStmtTable, Long fStmtKey, String refNo, String remarks, Timestamp timeUpdate, Integer userId,
			String currency, BigDecimal amount) throws Exception
	{
		String strErrMsg = null;
		// / checking input variables
		if (naPkid == null)
			throw new Exception(" Invalid Nominal Account PKID ");
		NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
		GenericStmtObject gsObj = new GenericStmtObject();
		gsObj.namespace = GenericStmtBean.NS_CUSTOMER;
		gsObj.dateStmt = stmtDate;
		// // assuming Credit Note here!!!!
		gsObj.glCodeDebit = GLCodeBean.ACC_RECEIVABLE;
		gsObj.glCodeCredit = glCodeCredit;
		// gsObj.currency = naObj.currency;
		gsObj.currency = currency;
		gsObj.amount = amount;
		gsObj.dateDue = stmtDate;
		gsObj.foreignEntityTable = naObj.foreignTable;
		gsObj.foreignEntityKey = naObj.foreignKey;
		gsObj.pcCenter = naObj.accPCCenterId;
		gsObj.stmtType = stmtType;
		gsObj.foreignStmtTable = fStmtTable;
		gsObj.foreignStmtKey = fStmtKey;
		gsObj.referenceNo = refNo;
		// gsObj.remarks = remarks;
		gsObj.remarks = "AUTO-DN: " + remarks;
		gsObj.dateStart = gsObj.dateStmt;
		gsObj.dateEnd = gsObj.dateStmt;
		gsObj.dateDue = gsObj.dateStmt;
		gsObj.dateCreated = gsObj.dateStmt;
		gsObj.dateApproved = gsObj.dateStmt;
		gsObj.dateVerified = gsObj.dateStmt;
		gsObj.dateVerified = gsObj.dateStmt;
		gsObj.dateUpdate = timeUpdate;
		gsObj.userIdCreate = userId;
		gsObj.userIdPIC = userId;
		gsObj.userIdApprove = userId;
		gsObj.userIdVerified = userId;
		gsObj.userIdUpdate = userId;
		// create a nominal account txn for this generic stmt
		NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		natObj.nominalAccount = naObj.pkid;
		natObj.foreignTable = GenericStmtBean.TABLENAME;
		natObj.description = gsObj.remarks;
		natObj.glCodeDebit = gsObj.glCodeDebit;
		natObj.glCodeCredit = gsObj.glCodeCredit;
		natObj.currency = gsObj.currency;
		// natObj.amount = gsObj.amount.negate();
		natObj.amount = gsObj.amount;
		natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
		natObj.timeParam1 = gsObj.dateStmt;
		natObj.state = NominalAccountTxnBean.ST_ACTUAL;
		natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
		natObj.lastUpdate = TimeFormat.getTimestamp();
		natObj.userIdUpdate = gsObj.userIdCreate;
		GenericStmt gsEJB = GenericStmtNut.fnCreate(gsObj);
		if (gsEJB == null)
		{
			throw new Exception(" Unable to create Generic Stmt Bean ");
		}
		natObj.foreignKey = gsObj.pkid;
		NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
		if (natEJB == null)
		{
			try
			{
				gsEJB.remove();
			} catch (Exception ex)
			{
				ex.printStackTrace();
				throw new Exception(" Unable to create Nominal Account Txn ");
			}
		}
		naObj.amount = naObj.amount.add(natObj.amount);
		return gsObj.pkid;
	}

	private Long createReversePayment(String strStmtType, Integer iNominalAccId, Integer iCashAccountId,
			String currency, BigDecimal bdPaymentAmt, String strChequeCreditCardNo, String strRemarks, String strInfo1,
			Timestamp tsDateStmt, String strForeignStmtTable, Long iSettleStmtId, Integer usrid) throws Exception
	{
		String strErrMsg = null;
		// first of all, get the for parameters
		// String strStmtType = (String) req.getParameter("stmtType");
		// String strNominalAcc = (String)req.getParameter("nominalAcc");
		// String strCashAccount = (String)req.getParameter("cashAccount");
		// String strAmount = (String)req.getParameter("amount");
		// BigDecimal bdPaymentAmt= new BigDecimal(strAmount);
		// String strAmtInWords = (String)req.getParameter("amtInWords");
		String strAmtInWords = CurrencyFormat.toWords(bdPaymentAmt, "", "");
		strAmtInWords = strAmtInWords + " ONLY";
		/*
		 * String strChequeCreditCardNo = (String)
		 * req.getParameter("chequeCreditCardNo"); String strRemarks =
		 * (String)req.getParameter("remarks"); String strInfo1 =
		 * (String)req.getParameter("info1");
		 * 
		 * String strDateStmt = (String) req.getParameter("dateStmt");
		 * 
		 * String foreignStmtTable = (String)
		 * req.getParameter("foreignStmtTable"); String strSettleStmtId =
		 * (String) req.getParameter("foreignStmtKey"); String userName =
		 * (String) req.getParameter("userName");
		 */
		// Get the cash account object
		CashAccountObject caObj = null;
		try
		{
			caObj = CashAccountNut.getObject(
			// new Integer(strCashAccount));
					iCashAccountId);
		} catch (Exception ex)
		{ /* do nothing */
		}
		if (caObj == null)
		{
			throw new Exception("Invalid Cash Account Object ");
		}
		// Get the nominal account object
		NominalAccountObject naObj = null;
		try
		{
			naObj = NominalAccountNut.getObject(
			// new Integer(strNominalAcc));
					iNominalAccId);
		} catch (Exception ex)
		{ /* do nothing */
		}
		if (naObj == null)
		{
			throw new Exception("Error fetching Nominal Account Object ");
		}
		GenericStmtObject gsObj = new GenericStmtObject();
		try
		{
			gsObj.namespace = GenericStmtBean.NS_CUSTOMER;
			gsObj.pcCenter = naObj.accPCCenterId;
			gsObj.glCodeDebit = GLCodeBean.ACC_RECEIVABLE;
			gsObj.glCodeCredit = caObj.accountType;
			// gsObj.currency = naObj.currency;
			gsObj.currency = currency;
			gsObj.amount = bdPaymentAmt;
			gsObj.cashFrom = caObj.pkId;
			gsObj.cashTo = caObj.pkId;
			gsObj.stmtType = strStmtType;
			// gsObj.foreignStmtTable = GenericStmtBean.TABLENAME;
			gsObj.foreignStmtTable = strForeignStmtTable;
			gsObj.foreignStmtKey = iSettleStmtId;
			gsObj.chequeCreditCardNo = strChequeCreditCardNo;
			gsObj.remarks = strRemarks;
			gsObj.info1 = strInfo1;
			gsObj.info2 = strAmtInWords;
			gsObj.nominalAccount = naObj.pkid;
			gsObj.foreignEntityTable = GenericEntityAccountBean.TABLENAME;
			gsObj.foreignEntityKey = naObj.foreignKey;
			gsObj.dateStmt = tsDateStmt;
			gsObj.dateStart = tsDateStmt;
			gsObj.dateEnd = tsDateStmt;
			gsObj.dateDue = tsDateStmt;
			gsObj.dateCreated = TimeFormat.getTimestamp();
			gsObj.dateApproved = TimeFormat.getTimestamp();
			gsObj.dateVerified = TimeFormat.getTimestamp();
			gsObj.dateUpdate = TimeFormat.getTimestamp();
			gsObj.userIdCreate = usrid;
			gsObj.userIdPIC = gsObj.userIdCreate;
			gsObj.userIdApprove = gsObj.userIdCreate;
			gsObj.userIdVerified = gsObj.userIdCreate;
			gsObj.userIdUpdate = gsObj.userIdCreate;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception(" Unable to create GenericStmtObject ");
		}
		// update the cash account txn
		CashAccTxnObject catObjF = new CashAccTxnObject();
		catObjF.txnCode = CashAccTxnBean.TXN_CODE_DEFAULT;
		catObjF.glCodeDebit = gsObj.glCodeDebit;
		catObjF.glCodeCredit = gsObj.glCodeCredit;
		catObjF.personInCharge = gsObj.userIdCreate;
		// catObjF.accFrom = caObjTo.pkId;
		catObjF.accTo = caObj.pkId;
		catObjF.foreignTable = CashAccTxnBean.FT_GENERIC_STMT;
		catObjF.foreignKey = gsObj.pkid;
		catObjF.currency = gsObj.currency;
		catObjF.amount = gsObj.amount.negate();
		catObjF.txnTime = gsObj.dateStmt;
		catObjF.remarks = gsObj.remarks;
		catObjF.state = CashAccTxnBean.ST_CREATED;
		catObjF.status = CashAccTxnBean.STATUS_ACTIVE;
		catObjF.lastUpdate = TimeFormat.getTimestamp();
		catObjF.userIdUpdate = gsObj.userIdCreate;
		catObjF.pcCenter = caObj.pcCenter;
		CashAccTxnNut.fnCreate(catObjF);
		// create a nominal account txn for this generic stmt
		NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		natObj.nominalAccount = naObj.pkid;
		natObj.foreignTable = GenericStmtBean.TABLENAME;
		// the natObj.foreignKey will be assigned later after gsObj
		// is created and a pkid is obtained
		natObj.description = gsObj.remarks;
		natObj.glCodeDebit = gsObj.glCodeDebit;
		natObj.glCodeCredit = gsObj.glCodeCredit;
		natObj.currency = gsObj.currency;
		natObj.amount = gsObj.amount;
		natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
		natObj.timeParam1 = gsObj.dateStmt;
		natObj.state = NominalAccountTxnBean.ST_ACTUAL;
		natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
		natObj.lastUpdate = TimeFormat.getTimestamp();
		natObj.userIdUpdate = gsObj.userIdCreate;
		GenericStmt gsEJB = GenericStmtNut.fnCreate(gsObj);
		if (gsEJB == null)
		{
			throw new Exception(" Unable to create Generic Stmt Bean ");
		}
		natObj.foreignKey = gsObj.pkid;
		NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
		if (natEJB == null)
		{
			try
			{
				gsEJB.remove();
			} catch (Exception ex)
			{
				ex.printStackTrace();
				throw new Exception(" Unable to create Nominal Account Txn ");
			}
		}
		naObj.amount = naObj.amount.add(natObj.amount);
		return gsObj.pkid;
	}

	private Vector getPaymentSum(Connection con, String payNo, String strCurr, String remarks, Integer usrid)
			throws Exception
	{
		// drop tmp table
		try
		{
			String dropTmpTable = "drop table tmp_payrefd";
			Statement dropTmpTableStmt = con.createStatement();
			dropTmpTableStmt.executeUpdate(dropTmpTable);
		} catch (Exception ex)
		{
			// ignore
		}
		String selectDistinct = "select distinct payno, txrefno, custid, lpayamt into tmp_payrefd "
				+ "from payrefd where payno = '" + payNo + "'";
		Log.printVerbose("selectDistinct = " + selectDistinct);
		String getPymtSumQ = "select custid, sum(lpayamt) from tmp_payrefd group by custid";
		Log.printVerbose("getPymtSumQ = " + getPymtSumQ);
		String strCustId = null;
		BigDecimal bdSumOfPayments = new BigDecimal(0);
		Vector vecRtn = new Vector();
		Statement selectDistinctStmt = con.createStatement();
		Statement getPymtSumStmt = con.createStatement();
		selectDistinctStmt.executeUpdate(selectDistinct);
		ResultSet rsGetPymtSum = getPymtSumStmt.executeQuery(getPymtSumQ);
		while (rsGetPymtSum.next())
		{
			Log.printVerbose("Found a hit ...");
			// try
			// {
			strCustId = rsGetPymtSum.getString(1);
			bdSumOfPayments = rsGetPymtSum.getBigDecimal(2);
			// derive the NominalAccount
			CustAccount thisCustAcc = CustAccountNut.getObjectByCode(strCustId);
			NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER, iDefPCCenterId,
					thisCustAcc.getPkid(), strCurr);
			if (naObj == null)
			{
				naObj = new NominalAccountObject();
				// naObj.pkid = new Integer("0");
				// naObj.code = new String("not_used");
				naObj.namespace = NominalAccountBean.NS_CUSTOMER;
				naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
				naObj.foreignKey = thisCustAcc.getPkid();
				naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
				naObj.currency = strCurr;
				// naObj.amount = naObj.amount.add(lTxAmt);
				naObj.remarks = remarks;
				naObj.accPCCenterId = iDefPCCenterId;
				naObj.state = NominalAccountBean.STATE_CREATED;
				naObj.status = NominalAccountBean.STATUS_ACTIVE;
				naObj.lastUpdate = TimeFormat.getTimestamp();
				naObj.userIdUpdate = usrid;
				NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
			}
			vecRtn.add(new Payment(naObj.pkid, bdSumOfPayments));
			// }
			// catch(Exception ex)
			// {
			// }
		} // end while
		selectDistinctStmt.close();
		getPymtSumStmt.close();
		return vecRtn;
	}

	private Vector getRevPaymentSum(Connection con, String docNo, String strCurr, String remarks, Integer usrid)
			throws Exception
	{
		String getPymtSumQ = "select custid, sum(lpayamt) from "
				+ "(select distinct payno, txrefno, custid, lpayamt from payrefd " + " where txrefno = '" + docNo
				+ "') as tmp_payrefd group by custid";
		Log.printVerbose("getRevPymtSumQ = " + getPymtSumQ);
		String strCustId = null;
		BigDecimal bdSumOfPayments = new BigDecimal(0);
		Vector vecRtn = new Vector();
		Statement getPymtSumStmt = con.createStatement();
		ResultSet rsGetPymtSum = getPymtSumStmt.executeQuery(getPymtSumQ);
		while (rsGetPymtSum.next())
		{
			Log.printVerbose("Found a hit ...");
			// try
			// {
			strCustId = rsGetPymtSum.getString(1);
			bdSumOfPayments = rsGetPymtSum.getBigDecimal(2);
			// derive the NominalAccount
			CustAccount thisCustAcc = CustAccountNut.getObjectByCode(strCustId);
			NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER, iDefPCCenterId,
					thisCustAcc.getPkid(), strCurr);
			if (naObj == null)
			{
				naObj = new NominalAccountObject();
				// naObj.pkid = new Integer("0");
				// naObj.code = new String("not_used");
				naObj.namespace = NominalAccountBean.NS_CUSTOMER;
				naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
				naObj.foreignKey = thisCustAcc.getPkid();
				naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
				naObj.currency = strCurr;
				// naObj.amount = naObj.amount.add(lTxAmt);
				naObj.remarks = remarks;
				naObj.accPCCenterId = iDefPCCenterId;
				naObj.state = NominalAccountBean.STATE_CREATED;
				naObj.status = NominalAccountBean.STATUS_ACTIVE;
				naObj.lastUpdate = TimeFormat.getTimestamp();
				naObj.userIdUpdate = usrid;
				NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
			}
			vecRtn.add(new Payment(naObj.pkid, bdSumOfPayments));
			// }
			// catch(Exception ex)
			// {
			// }
		} // end while
		getPymtSumStmt.close();
		return vecRtn;
	}

	/*
	 * private BigDecimal getPaymentSum(Connection con, String payNo) throws
	 * Exception { String getPymtSumQ = "select sum(lpayamt) from payrefd where
	 * payno = '" + payNo + "'";
	 * 
	 * BigDecimal bdSumOfPayments = new BigDecimal(0); Statement getPymtSumStmt =
	 * con.createStatement(); ResultSet rsGetPymtSum =
	 * getPymtSumStmt.executeQuery(getPymtSumQ); if (rsGetPymtSum.next()) { try {
	 * bdSumOfPayments = rsGetPymtSum.getBigDecimal(1); } catch(Exception ex) { }
	 * 
	 * if(bdSumOfPayments == null) bdSumOfPayments = new BigDecimal(0); }
	 * 
	 * return bdSumOfPayments; }
	 */
	/*
	 * private BigDecimal getRevPaymentSum(Connection con, String payNo) throws
	 * Exception { String getPymtSumQ = "select sum(lpayamt) from payrefd where
	 * txrefno = '" + payNo + "'";
	 * 
	 * BigDecimal bdSumOfPayments = new BigDecimal(0); Statement getPymtSumStmt =
	 * con.createStatement(); ResultSet rsGetPymtSum =
	 * getPymtSumStmt.executeQuery(getPymtSumQ); if (rsGetPymtSum.next()) { try {
	 * bdSumOfPayments = rsGetPymtSum.getBigDecimal(1); } catch(Exception ex) { }
	 * 
	 * if(bdSumOfPayments == null) bdSumOfPayments = new BigDecimal(0); }
	 * 
	 * return bdSumOfPayments; }
	 */
	private void fixExRate(Connection topconCon, Connection jbossCon, Integer usrid) throws Exception
	{
		String findInvQ = "select pkid from cust_invoice_index where remarks ~* ? ";
		PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		String findCurrQ = "select currid from cdhis where docno = ? ";
		PreparedStatement findCurrStmt = topconCon.prepareStatement(findCurrQ);
		String queryXRateGainLose = "select * from payrefd where gainloseamt != 0 ";
		Statement xRateGainLoseStmt = topconCon.createStatement();
		ResultSet rsXRateGainLose = xRateGainLoseStmt.executeQuery(queryXRateGainLose);
		int count = 0;
		while (rsXRateGainLose.next())
		{
			Log.printVerbose("*** Processing Txn " + ++count);
			String strPayNo = rsXRateGainLose.getString("payno");
			String strTxRefNo = rsXRateGainLose.getString("txrefno");
			String strCustCode = rsXRateGainLose.getString("custid");
			Timestamp payDate = TimeFormat.createTimeStamp(rsXRateGainLose.getString("paydate"), "MM/dd/yy HH:mm:ss");
			BigDecimal bdPayExchRate = rsXRateGainLose.getBigDecimal("exch_rate");
			BigDecimal bdTxExchRate = rsXRateGainLose.getBigDecimal("txexch_rate");
			BigDecimal bdGainLoseAmt = rsXRateGainLose.getBigDecimal("gainloseamt");
			// Get the Invoice Id
			Long invId = new Long(0);
			findInvStmt.setString(1, " = " + strTxRefNo + ")");
			ResultSet rsFindInv = findInvStmt.executeQuery();
			if (rsFindInv.next())
			{
				invId = new Long(rsFindInv.getLong("pkid"));
			}
			// Get the Customer Id
			Integer custId = null;
			//findCustStmt.setString(1, strCustCode);
			findCustStmt.setString(1, strCustCode);
			ResultSet rsFindCust = findCustStmt.executeQuery();
			if (rsFindCust.next())
			{
				custId = new Integer(rsFindCust.getInt("pkid"));
			}
			// Get the Currency
			String strCurr = null;
			findCurrStmt.setString(1, strPayNo);
			ResultSet rsFindCurr = findCurrStmt.executeQuery();
			if (rsFindCurr.next())
			{
				strCurr = rsFindCurr.getString("currid");
				strCurr = (String) hmCurr.get(new Integer(strCurr));
			}
			// Get the Nominal Account
			NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER, iDefPCCenterId,
					custId, "MYR");
			if (naObj == null)
			{
				naObj = new NominalAccountObject();
				// naObj.pkid = new Integer("0");
				// naObj.code = new String("not_used");
				naObj.namespace = NominalAccountBean.NS_CUSTOMER;
				naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
				naObj.foreignKey = custId;
				naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
				// naObj.currency = thisCurr;
				naObj.currency = "MYR";
				// naObj.amount = naObj.amount.add(lTxAmt);
				// naObj.remarks = remarks;
				naObj.accPCCenterId = iDefPCCenterId;
				naObj.state = NominalAccountBean.STATE_CREATED;
				naObj.status = NominalAccountBean.STATUS_ACTIVE;
				naObj.lastUpdate = TimeFormat.getTimestamp();
				naObj.userIdUpdate = usrid;
				NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
			}
			String strRemarks = "EXCH_RATE OFFSET (txexch_rate=" + bdTxExchRate.toString() + ", pay_exch_rate="
					+ bdPayExchRate.toString() + ")";
			// if bdGainLoseAmt < 0, means we need to CN to offset the balance
			// if bdGainLoseAmt > 0, means we need to DN to offset the balance
			if (bdGainLoseAmt.signum() < 0)
			{
				Log.printVerbose("*** " + count + ": Creating CN");
				Long newCNId = createCreditNote(naObj.pkid, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
						GLCodeBean.CASH_DISCOUNT, payDate, InvoiceBean.TABLENAME, invId, strPayNo, strRemarks,
						TimeFormat.getTimestamp(), usrid, strCurr, bdGainLoseAmt.abs());
			} else
			{
				// Create a Debit Note
				Log.printVerbose("*** " + count + ": Creating DN");
				Long newDNId = createDebitNote(naObj.pkid, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
						GLCodeBean.INTEREST_REVENUE, payDate, InvoiceBean.TABLENAME, invId, strPayNo, strRemarks,
						TimeFormat.getTimestamp(), usrid, strCurr, bdGainLoseAmt.abs());
			}
		} // end while
	} // end fixExRate
	// internal class
	class Payment
	{
		public BigDecimal amt;
		public Integer nomAccId;

		Payment(Integer nomAccId, BigDecimal amt)
		{
			this.amt = amt;
			this.nomAccId = nomAccId;
		}
	}
}
