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
import com.vlee.ejb.accounting.*;

/**
 * Fixes Payments from Customer that exceeds the Invoice Amt. Fix the receipts
 * to reflect the overpaid amount, instead of the amount that will negate the
 * invoice.
 * 
 */
public class DoTopConFixOverPymt implements Action
{
	private String strClassName = "DoTopConFixOverPymt";
	private static Task curTask = null;
	/***************************************************************************
	 * GLOBAL VARIABLES
	 **************************************************************************/
	Integer iDefLocId = new Integer(1000);
	Integer iDefSvcCtrId = new Integer(1);
	Integer iDefPCCenterId = new Integer(1000);
	Integer iDefCondId = new Integer(StockNut.STK_COND_GOOD);
	Integer iDefCashAccId = new Integer(1000);
	String strDefCurr = "MYR";

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
		HashMap hmCurr = new HashMap();
		hmCurr.put(new Integer(1), "MYR");
		hmCurr.put(new Integer(2), "USD");
		hmCurr.put(new Integer(3), "SGD");
		hmCurr.put(new Integer(4), "4");
		hmCurr.put(new Integer(5), "5");
		hmCurr.put(new Integer(6), "6");
		hmCurr.put(new Integer(7), "7");
		try
		{
			// Connection to topcon DB
			String url = "jdbc:postgresql://localhost:5432/topcon";
			Connection con = DriverManager.getConnection(url, "jboss", "jboss");
			// Connection to Wavelet EMP DB
			String jbossURL = "jdbc:postgresql://localhost:5432/wsemp";
			Connection jbossCon = DriverManager.getConnection(jbossURL, "jboss", "jboss");
			Statement topconStmt = con.createStatement();
			Statement jbossStmt = jbossCon.createStatement();
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
			String queryOverPymt = "select docno,ldebitamt,lcreditamt,custid from cdhis"
					+ " where txtype = 'P' and ldebitamt != lcreditamt" + " and custid != 'MC0103' and ldebitamt != 0"
					+ " order by custid";
			// NOTE: We deal with MC0103 in Fix Misc.
			Statement queryOverPymtStmt = con.createStatement();
			ResultSet rsOverPymt = queryOverPymtStmt.executeQuery(queryOverPymt);
			/*******************************************************************
			 * LOGIC: ====== For each overpayment, (1) Find the receipt whose
			 * payment_remarks ~* docno, (2) Correct the amount (use lcreditamt
			 * instead of ldebitamt) (3) Fix the corresponding NominalAccountTxn
			 * whose foreign_table='acc_receipt_index' and foreign_key=<Receipt
			 * PKID> (4) Fix corresponding acc_cash_transactions where
			 * foreign_table='acc_receipt_index' and foreign_key=<Receipt PKID>
			 * (5) Fix NominalAccount whose PKID = nominal_account of (3)
			 */
			String findRcptQ =
			// "select pkid from cust_receipt_index where payment_remarks ~* ?
			// ";
			// "select pkid from acc_receipt_index where payment_remarks ~* ? ";
			"select pkid from acc_receipt_index where payment_remarks ILIKE ? ";
			PreparedStatement findRcptStmt = jbossCon.prepareStatement(findRcptQ);
			String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
			PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
			curTask = new Task("Fix Overpayment", rsOverPymt.getFetchSize());
			int count = 0;
			while (rsOverPymt.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printDebug("*** Processing Txn " + ++count);
				String strDocNo = rsOverPymt.getString("docno");
				String strCustCode = rsOverPymt.getString("custid");
				BigDecimal bdLDebitAmt = rsOverPymt.getBigDecimal("ldebitamt");
				BigDecimal bdLCreditAmt = rsOverPymt.getBigDecimal("lcreditamt");
				// Get the Customer Id
				Integer custId = null;
				findCustStmt.setString(1, strCustCode);
				ResultSet rsFindCust = findCustStmt.executeQuery();
				if (rsFindCust.next())
				{
					custId = new Integer(rsFindCust.getInt("pkid"));
				}
				// Get the Receipt PKID
				Long rcptId = new Long(0);
				findRcptStmt.setString(1, "% = " + strDocNo + ")%");
				ResultSet rsFindRcpt = findRcptStmt.executeQuery();
				if (rsFindRcpt.getFetchSize() > 1)
					throw new Exception("Duplicate Receipt Found for " + strDocNo);
				if (rsFindRcpt.next())
				{
					rcptId = new Long(rsFindRcpt.getLong("pkid"));
				}
				String strAmount = CurrencyFormat.toWords(bdLCreditAmt, "", "");
				strAmount = strAmount + " ONLY";
				Log.printDebug("Updating Receipt(" + rcptId + ") with " + bdLCreditAmt + "(" + strAmount + ")");
				OfficialReceipt thisRcpt = OfficialReceiptNut.getHandle(rcptId);
				OfficialReceiptObject thisRcptObj = thisRcpt.getObject();
				thisRcptObj.amount = bdLCreditAmt;
				// thisRcpt.amountStr(strAmount);
				if (thisRcptObj.paymentMethod.equals("cheque"))
				{
					thisRcptObj.amountCheque = bdLCreditAmt;
				} else
				{
					thisRcptObj.amountCash = bdLCreditAmt;
				}
				thisRcpt.setObject(thisRcptObj);
				/*
				 * CashTxn wasn't created properly, uncomment this once CashTxn
				 * is populated Vector vecCashTxnObj =
				 * CashAccTxnNut.getValueObjectsGiven(
				 * CashAccTxnBean.FOREIGN_TABLE,OfficialReceiptBean.TABLENAME,
				 * CashAccTxnBean.FOREIGN_KEY,rcptId.toString(), null, null); if
				 * (vecCashTxnObj == null || vecCashTxnObj.size() == 0) throw
				 * new Exception("CashTxn Not Found for " + strDocNo); if
				 * (vecCashTxnObj.size() > 1) throw new Exception("Duplicate
				 * CashTxn found for " + strDocNo); CashAccTxnObject thisCATObj =
				 * (CashAccTxnObject) vecCashTxnObj.get(0); CashAccTxn thisCAT =
				 * CashAccTxnNut.getHandle(thisCATObj.pkid);
				 * thisCAT.setAmount(bdLCreditAmt);
				 */
				Vector vecNATObj = NominalAccountTxnNut.getValueObjectsGiven(NominalAccountTxnBean.FOREIGN_TABLE,
						OfficialReceiptBean.TABLENAME, NominalAccountTxnBean.FOREIGN_KEY, rcptId.toString());
				if (vecNATObj == null || vecNATObj.size() == 0)
					throw new Exception("NAT Not Found for " + strDocNo);
				if (vecNATObj.size() > 1)
					throw new Exception("Duplicate NAT found for " + strDocNo);
				NominalAccountTxnObject thisNATObj = (NominalAccountTxnObject) vecNATObj.get(0);
				NominalAccountTxn thisNAT = NominalAccountTxnNut.getHandle(thisNATObj.pkid);
				BigDecimal bdOldNATAmt = thisNAT.getAmount();
				thisNAT.setAmount(bdLCreditAmt.negate());
				NominalAccount thisNA = NominalAccountNut.getHandle(thisNATObj.nominalAccount);
				// thisNA.setAmount(thisNA.getAmount().subtract(bdOldNATAmt).subtract(bdLCreditAmt));
				thisNA.addAmount(bdOldNATAmt.negate());
				thisNA.addAmount(bdLCreditAmt.negate());
			} // while rsOverPymt.next()
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred : " + ex.getMessage());
		}
		Log.printVerbose("***** END: FIX OVER-PAYMENT *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "FIX OVER-PAYMENT");
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
import com.vlee.ejb.accounting.*;

/**
 * Fixes Payments from Customer that exceeds the Invoice Amt. Fix the receipts
 * to reflect the overpaid amount, instead of the amount that will negate the
 * invoice.
 * 
 */
public class DoTopConFixOverPymt implements Action
{
	private String strClassName = "DoTopConFixOverPymt";
	private static Task curTask = null;
	/***************************************************************************
	 * GLOBAL VARIABLES
	 **************************************************************************/
	Integer iDefLocId = new Integer(1000);
	Integer iDefSvcCtrId = new Integer(1);
	Integer iDefPCCenterId = new Integer(1000);
	Integer iDefCondId = new Integer(StockNut.STK_COND_GOOD);
	Integer iDefCashAccId = new Integer(1000);
	String strDefCurr = "MYR";

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
		HashMap hmCurr = new HashMap();
		hmCurr.put(new Integer(1), "MYR");
		hmCurr.put(new Integer(2), "USD");
		hmCurr.put(new Integer(3), "SGD");
		hmCurr.put(new Integer(4), "4");
		hmCurr.put(new Integer(5), "5");
		hmCurr.put(new Integer(6), "6");
		hmCurr.put(new Integer(7), "7");
		try
		{
			// Connection to topcon DB
			String url = "jdbc:postgresql://localhost:5432/topcon";
			Connection con = DriverManager.getConnection(url, "jboss", "jboss");
			// Connection to Wavelet EMP DB
			String jbossURL = "jdbc:postgresql://localhost:5432/wsemp";
			Connection jbossCon = DriverManager.getConnection(jbossURL, "jboss", "jboss");
			Statement topconStmt = con.createStatement();
			Statement jbossStmt = jbossCon.createStatement();
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
			String queryOverPymt = "select docno,ldebitamt,lcreditamt,custid from cdhis"
					+ " where txtype = 'P' and ldebitamt != lcreditamt" + " and custid != 'MC0103' and ldebitamt != 0"
					+ " order by custid";
			// NOTE: We deal with MC0103 in Fix Misc.
			Statement queryOverPymtStmt = con.createStatement();
			ResultSet rsOverPymt = queryOverPymtStmt.executeQuery(queryOverPymt);
			/*******************************************************************
			 * LOGIC: ====== For each overpayment, (1) Find the receipt whose
			 * payment_remarks ~* docno, (2) Correct the amount (use lcreditamt
			 * instead of ldebitamt) (3) Fix the corresponding NominalAccountTxn
			 * whose foreign_table='acc_receipt_index' and foreign_key=<Receipt
			 * PKID> (4) Fix corresponding acc_cash_transactions where
			 * foreign_table='acc_receipt_index' and foreign_key=<Receipt PKID>
			 * (5) Fix NominalAccount whose PKID = nominal_account of (3)
			 */
			String findRcptQ =
			// "select pkid from cust_receipt_index where payment_remarks ~* ?
			// ";
			// "select pkid from acc_receipt_index where payment_remarks ~* ? ";
			"select pkid from acc_receipt_index where payment_remarks ILIKE ? ";
			PreparedStatement findRcptStmt = jbossCon.prepareStatement(findRcptQ);
			String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
			PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
			curTask = new Task("Fix Overpayment", rsOverPymt.getFetchSize());
			int count = 0;
			while (rsOverPymt.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printDebug("*** Processing Txn " + ++count);
				String strDocNo = rsOverPymt.getString("docno");
				String strCustCode = rsOverPymt.getString("custid");
				BigDecimal bdLDebitAmt = rsOverPymt.getBigDecimal("ldebitamt");
				BigDecimal bdLCreditAmt = rsOverPymt.getBigDecimal("lcreditamt");
				// Get the Customer Id
				Integer custId = null;
				findCustStmt.setString(1, strCustCode);
				ResultSet rsFindCust = findCustStmt.executeQuery();
				if (rsFindCust.next())
				{
					custId = new Integer(rsFindCust.getInt("pkid"));
				}
				// Get the Receipt PKID
				Long rcptId = new Long(0);
				findRcptStmt.setString(1, "% = " + strDocNo + ")%");
				ResultSet rsFindRcpt = findRcptStmt.executeQuery();
				if (rsFindRcpt.getFetchSize() > 1)
					throw new Exception("Duplicate Receipt Found for " + strDocNo);
				if (rsFindRcpt.next())
				{
					rcptId = new Long(rsFindRcpt.getLong("pkid"));
				}
				String strAmount = CurrencyFormat.toWords(bdLCreditAmt, "", "");
				strAmount = strAmount + " ONLY";
				Log.printDebug("Updating Receipt(" + rcptId + ") with " + bdLCreditAmt + "(" + strAmount + ")");
				OfficialReceipt thisRcpt = OfficialReceiptNut.getHandle(rcptId);
				OfficialReceiptObject thisRcptObj = thisRcpt.getObject();
				thisRcptObj.amount = bdLCreditAmt;
				// thisRcpt.amountStr(strAmount);
				if (thisRcptObj.paymentMethod.equals("cheque"))
				{
					thisRcptObj.amountCheque = bdLCreditAmt;
				} else
				{
					thisRcptObj.amountCash = bdLCreditAmt;
				}
				thisRcpt.setObject(thisRcptObj);
				/*
				 * CashTxn wasn't created properly, uncomment this once CashTxn
				 * is populated Vector vecCashTxnObj =
				 * CashAccTxnNut.getValueObjectsGiven(
				 * CashAccTxnBean.FOREIGN_TABLE,OfficialReceiptBean.TABLENAME,
				 * CashAccTxnBean.FOREIGN_KEY,rcptId.toString(), null, null); if
				 * (vecCashTxnObj == null || vecCashTxnObj.size() == 0) throw
				 * new Exception("CashTxn Not Found for " + strDocNo); if
				 * (vecCashTxnObj.size() > 1) throw new Exception("Duplicate
				 * CashTxn found for " + strDocNo); CashAccTxnObject thisCATObj =
				 * (CashAccTxnObject) vecCashTxnObj.get(0); CashAccTxn thisCAT =
				 * CashAccTxnNut.getHandle(thisCATObj.pkid);
				 * thisCAT.setAmount(bdLCreditAmt);
				 */
				Vector vecNATObj = NominalAccountTxnNut.getValueObjectsGiven(NominalAccountTxnBean.FOREIGN_TABLE,
						OfficialReceiptBean.TABLENAME, NominalAccountTxnBean.FOREIGN_KEY, rcptId.toString());
				if (vecNATObj == null || vecNATObj.size() == 0)
					throw new Exception("NAT Not Found for " + strDocNo);
				if (vecNATObj.size() > 1)
					throw new Exception("Duplicate NAT found for " + strDocNo);
				NominalAccountTxnObject thisNATObj = (NominalAccountTxnObject) vecNATObj.get(0);
				NominalAccountTxn thisNAT = NominalAccountTxnNut.getHandle(thisNATObj.pkid);
				BigDecimal bdOldNATAmt = thisNAT.getAmount();
				thisNAT.setAmount(bdLCreditAmt.negate());
				NominalAccount thisNA = NominalAccountNut.getHandle(thisNATObj.nominalAccount);
				// thisNA.setAmount(thisNA.getAmount().subtract(bdOldNATAmt).subtract(bdLCreditAmt));
				thisNA.addAmount(bdOldNATAmt.negate());
				thisNA.addAmount(bdLCreditAmt.negate());
			} // while rsOverPymt.next()
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred : " + ex.getMessage());
		}
		Log.printVerbose("***** END: FIX OVER-PAYMENT *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "FIX OVER-PAYMENT");
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
import com.vlee.ejb.accounting.*;

/**
 * Fixes Payments from Customer that exceeds the Invoice Amt. Fix the receipts
 * to reflect the overpaid amount, instead of the amount that will negate the
 * invoice.
 * 
 */
public class DoTopConFixOverPymt implements Action
{
	private String strClassName = "DoTopConFixOverPymt";
	private static Task curTask = null;
	/***************************************************************************
	 * GLOBAL VARIABLES
	 **************************************************************************/
	Integer iDefLocId = new Integer(1000);
	Integer iDefSvcCtrId = new Integer(1);
	Integer iDefPCCenterId = new Integer(1000);
	Integer iDefCondId = new Integer(StockNut.STK_COND_GOOD);
	Integer iDefCashAccId = new Integer(1000);
	String strDefCurr = "MYR";

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
		HashMap hmCurr = new HashMap();
		hmCurr.put(new Integer(1), "MYR");
		hmCurr.put(new Integer(2), "USD");
		hmCurr.put(new Integer(3), "SGD");
		hmCurr.put(new Integer(4), "4");
		hmCurr.put(new Integer(5), "5");
		hmCurr.put(new Integer(6), "6");
		hmCurr.put(new Integer(7), "7");
		try
		{
			// Connection to topcon DB
			String url = "jdbc:postgresql://localhost:5432/topcon";
			Connection con = DriverManager.getConnection(url, "jboss", "jboss");
			// Connection to Wavelet EMP DB
			String jbossURL = "jdbc:postgresql://localhost:5432/wsemp";
			Connection jbossCon = DriverManager.getConnection(jbossURL, "jboss", "jboss");
			Statement topconStmt = con.createStatement();
			Statement jbossStmt = jbossCon.createStatement();
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
			String queryOverPymt = "select docno,ldebitamt,lcreditamt,custid from cdhis"
					+ " where txtype = 'P' and ldebitamt != lcreditamt" + " and custid != 'MC0103' and ldebitamt != 0"
					+ " order by custid";
			// NOTE: We deal with MC0103 in Fix Misc.
			Statement queryOverPymtStmt = con.createStatement();
			ResultSet rsOverPymt = queryOverPymtStmt.executeQuery(queryOverPymt);
			/*******************************************************************
			 * LOGIC: ====== For each overpayment, (1) Find the receipt whose
			 * payment_remarks ~* docno, (2) Correct the amount (use lcreditamt
			 * instead of ldebitamt) (3) Fix the corresponding NominalAccountTxn
			 * whose foreign_table='acc_receipt_index' and foreign_key=<Receipt
			 * PKID> (4) Fix corresponding acc_cash_transactions where
			 * foreign_table='acc_receipt_index' and foreign_key=<Receipt PKID>
			 * (5) Fix NominalAccount whose PKID = nominal_account of (3)
			 */
			String findRcptQ =
			// "select pkid from cust_receipt_index where payment_remarks ~* ?
			// ";
			// "select pkid from acc_receipt_index where payment_remarks ~* ? ";
			"select pkid from acc_receipt_index where payment_remarks ILIKE ? ";
			PreparedStatement findRcptStmt = jbossCon.prepareStatement(findRcptQ);
			String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
			PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
			curTask = new Task("Fix Overpayment", rsOverPymt.getFetchSize());
			int count = 0;
			while (rsOverPymt.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printDebug("*** Processing Txn " + ++count);
				String strDocNo = rsOverPymt.getString("docno");
				String strCustCode = rsOverPymt.getString("custid");
				BigDecimal bdLDebitAmt = rsOverPymt.getBigDecimal("ldebitamt");
				BigDecimal bdLCreditAmt = rsOverPymt.getBigDecimal("lcreditamt");
				// Get the Customer Id
				Integer custId = null;
				findCustStmt.setString(1, strCustCode);
				ResultSet rsFindCust = findCustStmt.executeQuery();
				if (rsFindCust.next())
				{
					custId = new Integer(rsFindCust.getInt("pkid"));
				}
				// Get the Receipt PKID
				Long rcptId = new Long(0);
				findRcptStmt.setString(1, "% = " + strDocNo + ")%");
				ResultSet rsFindRcpt = findRcptStmt.executeQuery();
				if (rsFindRcpt.getFetchSize() > 1)
					throw new Exception("Duplicate Receipt Found for " + strDocNo);
				if (rsFindRcpt.next())
				{
					rcptId = new Long(rsFindRcpt.getLong("pkid"));
				}
				String strAmount = CurrencyFormat.toWords(bdLCreditAmt, "", "");
				strAmount = strAmount + " ONLY";
				Log.printDebug("Updating Receipt(" + rcptId + ") with " + bdLCreditAmt + "(" + strAmount + ")");
				OfficialReceipt thisRcpt = OfficialReceiptNut.getHandle(rcptId);
				OfficialReceiptObject thisRcptObj = thisRcpt.getObject();
				thisRcptObj.amount = bdLCreditAmt;
				// thisRcpt.amountStr(strAmount);
				if (thisRcptObj.paymentMethod.equals("cheque"))
				{
					thisRcptObj.amountCheque = bdLCreditAmt;
				} else
				{
					thisRcptObj.amountCash = bdLCreditAmt;
				}
				thisRcpt.setObject(thisRcptObj);
				/*
				 * CashTxn wasn't created properly, uncomment this once CashTxn
				 * is populated Vector vecCashTxnObj =
				 * CashAccTxnNut.getValueObjectsGiven(
				 * CashAccTxnBean.FOREIGN_TABLE,OfficialReceiptBean.TABLENAME,
				 * CashAccTxnBean.FOREIGN_KEY,rcptId.toString(), null, null); if
				 * (vecCashTxnObj == null || vecCashTxnObj.size() == 0) throw
				 * new Exception("CashTxn Not Found for " + strDocNo); if
				 * (vecCashTxnObj.size() > 1) throw new Exception("Duplicate
				 * CashTxn found for " + strDocNo); CashAccTxnObject thisCATObj =
				 * (CashAccTxnObject) vecCashTxnObj.get(0); CashAccTxn thisCAT =
				 * CashAccTxnNut.getHandle(thisCATObj.pkid);
				 * thisCAT.setAmount(bdLCreditAmt);
				 */
				Vector vecNATObj = NominalAccountTxnNut.getValueObjectsGiven(NominalAccountTxnBean.FOREIGN_TABLE,
						OfficialReceiptBean.TABLENAME, NominalAccountTxnBean.FOREIGN_KEY, rcptId.toString());
				if (vecNATObj == null || vecNATObj.size() == 0)
					throw new Exception("NAT Not Found for " + strDocNo);
				if (vecNATObj.size() > 1)
					throw new Exception("Duplicate NAT found for " + strDocNo);
				NominalAccountTxnObject thisNATObj = (NominalAccountTxnObject) vecNATObj.get(0);
				NominalAccountTxn thisNAT = NominalAccountTxnNut.getHandle(thisNATObj.pkid);
				BigDecimal bdOldNATAmt = thisNAT.getAmount();
				thisNAT.setAmount(bdLCreditAmt.negate());
				NominalAccount thisNA = NominalAccountNut.getHandle(thisNATObj.nominalAccount);
				// thisNA.setAmount(thisNA.getAmount().subtract(bdOldNATAmt).subtract(bdLCreditAmt));
				thisNA.addAmount(bdOldNATAmt.negate());
				thisNA.addAmount(bdLCreditAmt.negate());
			} // while rsOverPymt.next()
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred : " + ex.getMessage());
		}
		Log.printVerbose("***** END: FIX OVER-PAYMENT *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "FIX OVER-PAYMENT");
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
import com.vlee.ejb.accounting.*;

/**
 * Fixes Payments from Customer that exceeds the Invoice Amt. Fix the receipts
 * to reflect the overpaid amount, instead of the amount that will negate the
 * invoice.
 * 
 */
public class DoTopConFixOverPymt implements Action
{
	private String strClassName = "DoTopConFixOverPymt";
	private static Task curTask = null;
	/***************************************************************************
	 * GLOBAL VARIABLES
	 **************************************************************************/
	Integer iDefLocId = new Integer(1000);
	Integer iDefSvcCtrId = new Integer(1);
	Integer iDefPCCenterId = new Integer(1000);
	Integer iDefCondId = new Integer(StockNut.STK_COND_GOOD);
	Integer iDefCashAccId = new Integer(1000);
	String strDefCurr = "MYR";

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
		HashMap hmCurr = new HashMap();
		hmCurr.put(new Integer(1), "MYR");
		hmCurr.put(new Integer(2), "USD");
		hmCurr.put(new Integer(3), "SGD");
		hmCurr.put(new Integer(4), "4");
		hmCurr.put(new Integer(5), "5");
		hmCurr.put(new Integer(6), "6");
		hmCurr.put(new Integer(7), "7");
		try
		{
			// Connection to topcon DB
			String url = "jdbc:postgresql://localhost:5432/topcon";
			Connection con = DriverManager.getConnection(url, "jboss", "jboss");
			// Connection to Wavelet EMP DB
			String jbossURL = "jdbc:postgresql://localhost:5432/wsemp";
			Connection jbossCon = DriverManager.getConnection(jbossURL, "jboss", "jboss");
			Statement topconStmt = con.createStatement();
			Statement jbossStmt = jbossCon.createStatement();
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
			String queryOverPymt = "select docno,ldebitamt,lcreditamt,custid from cdhis"
					+ " where txtype = 'P' and ldebitamt != lcreditamt" + " and custid != 'MC0103' and ldebitamt != 0"
					+ " order by custid";
			// NOTE: We deal with MC0103 in Fix Misc.
			Statement queryOverPymtStmt = con.createStatement();
			ResultSet rsOverPymt = queryOverPymtStmt.executeQuery(queryOverPymt);
			/*******************************************************************
			 * LOGIC: ====== For each overpayment, (1) Find the receipt whose
			 * payment_remarks ~* docno, (2) Correct the amount (use lcreditamt
			 * instead of ldebitamt) (3) Fix the corresponding NominalAccountTxn
			 * whose foreign_table='acc_receipt_index' and foreign_key=<Receipt
			 * PKID> (4) Fix corresponding acc_cash_transactions where
			 * foreign_table='acc_receipt_index' and foreign_key=<Receipt PKID>
			 * (5) Fix NominalAccount whose PKID = nominal_account of (3)
			 */
			String findRcptQ =
			// "select pkid from cust_receipt_index where payment_remarks ~* ?
			// ";
			// "select pkid from acc_receipt_index where payment_remarks ~* ? ";
			"select pkid from acc_receipt_index where payment_remarks ILIKE ? ";
			PreparedStatement findRcptStmt = jbossCon.prepareStatement(findRcptQ);
			String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
			PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
			curTask = new Task("Fix Overpayment", rsOverPymt.getFetchSize());
			int count = 0;
			while (rsOverPymt.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printDebug("*** Processing Txn " + ++count);
				String strDocNo = rsOverPymt.getString("docno");
				String strCustCode = rsOverPymt.getString("custid");
				BigDecimal bdLDebitAmt = rsOverPymt.getBigDecimal("ldebitamt");
				BigDecimal bdLCreditAmt = rsOverPymt.getBigDecimal("lcreditamt");
				// Get the Customer Id
				Integer custId = null;
				findCustStmt.setString(1, strCustCode);
				ResultSet rsFindCust = findCustStmt.executeQuery();
				if (rsFindCust.next())
				{
					custId = new Integer(rsFindCust.getInt("pkid"));
				}
				// Get the Receipt PKID
				Long rcptId = new Long(0);
				findRcptStmt.setString(1, "% = " + strDocNo + ")%");
				ResultSet rsFindRcpt = findRcptStmt.executeQuery();
				if (rsFindRcpt.getFetchSize() > 1)
					throw new Exception("Duplicate Receipt Found for " + strDocNo);
				if (rsFindRcpt.next())
				{
					rcptId = new Long(rsFindRcpt.getLong("pkid"));
				}
				String strAmount = CurrencyFormat.toWords(bdLCreditAmt, "", "");
				strAmount = strAmount + " ONLY";
				Log.printDebug("Updating Receipt(" + rcptId + ") with " + bdLCreditAmt + "(" + strAmount + ")");
				OfficialReceipt thisRcpt = OfficialReceiptNut.getHandle(rcptId);
				OfficialReceiptObject thisRcptObj = thisRcpt.getObject();
				thisRcptObj.amount = bdLCreditAmt;
				// thisRcpt.amountStr(strAmount);
				if (thisRcptObj.paymentMethod.equals("cheque"))
				{
					thisRcptObj.amountCheque = bdLCreditAmt;
				} else
				{
					thisRcptObj.amountCash = bdLCreditAmt;
				}
				thisRcpt.setObject(thisRcptObj);
				/*
				 * CashTxn wasn't created properly, uncomment this once CashTxn
				 * is populated Vector vecCashTxnObj =
				 * CashAccTxnNut.getValueObjectsGiven(
				 * CashAccTxnBean.FOREIGN_TABLE,OfficialReceiptBean.TABLENAME,
				 * CashAccTxnBean.FOREIGN_KEY,rcptId.toString(), null, null); if
				 * (vecCashTxnObj == null || vecCashTxnObj.size() == 0) throw
				 * new Exception("CashTxn Not Found for " + strDocNo); if
				 * (vecCashTxnObj.size() > 1) throw new Exception("Duplicate
				 * CashTxn found for " + strDocNo); CashAccTxnObject thisCATObj =
				 * (CashAccTxnObject) vecCashTxnObj.get(0); CashAccTxn thisCAT =
				 * CashAccTxnNut.getHandle(thisCATObj.pkid);
				 * thisCAT.setAmount(bdLCreditAmt);
				 */
				Vector vecNATObj = NominalAccountTxnNut.getValueObjectsGiven(NominalAccountTxnBean.FOREIGN_TABLE,
						OfficialReceiptBean.TABLENAME, NominalAccountTxnBean.FOREIGN_KEY, rcptId.toString());
				if (vecNATObj == null || vecNATObj.size() == 0)
					throw new Exception("NAT Not Found for " + strDocNo);
				if (vecNATObj.size() > 1)
					throw new Exception("Duplicate NAT found for " + strDocNo);
				NominalAccountTxnObject thisNATObj = (NominalAccountTxnObject) vecNATObj.get(0);
				NominalAccountTxn thisNAT = NominalAccountTxnNut.getHandle(thisNATObj.pkid);
				BigDecimal bdOldNATAmt = thisNAT.getAmount();
				thisNAT.setAmount(bdLCreditAmt.negate());
				NominalAccount thisNA = NominalAccountNut.getHandle(thisNATObj.nominalAccount);
				// thisNA.setAmount(thisNA.getAmount().subtract(bdOldNATAmt).subtract(bdLCreditAmt));
				thisNA.addAmount(bdOldNATAmt.negate());
				thisNA.addAmount(bdLCreditAmt.negate());
			} // while rsOverPymt.next()
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred : " + ex.getMessage());
		}
		Log.printVerbose("***** END: FIX OVER-PAYMENT *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "FIX OVER-PAYMENT");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		// Clean up curTask;
		curTask = null;
		return new ActionRouter("test-migrate-topcondb-page");
	}
}
