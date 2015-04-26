package com.vlee.servlet.test;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;

//import com.vlee.ejb.inventory.*;
/**
 * 
 * DoTopConSyncAR ================= There are discrepancies between the migrated
 * Nominal Account Balances and TOPCON's excel AR report, since the Topcon DB
 * contains missing/duplicated/erroneous data To sync up the migrated data with
 * that of Topcon's "trusted" balances, the best we could do, since we have the
 * AR Outstanding Balances from TOPCON (assumed to be CORRECT !!), is to
 * programmatically insert either a DN/CN to offset the discrepancies so that
 * they be equal.
 * 
 */
public class DoTopConSyncAR implements Action
{
	private String strClassName = "DoTopConSyncAR";
	private static Task curTask = null;
	/***************************************************************************
	 * GLOBAL VARIABLES
	 **************************************************************************/
	Integer iDefLocId = new Integer(1000);
	Integer iDefSvcCtrId = new Integer(1);
	Integer iDefPCCenterId = new Integer(1000);
	Integer iDefCashAccId = new Integer(1000);
	String strDefCurr = "MYR";

	/*
	 * HashMap hmCurr = new HashMap(); hmCurr.put(new Integer(1), "MYR");
	 * hmCurr.put(new Integer(2), "USD"); hmCurr.put(new Integer(3), "SGD");
	 * hmCurr.put(new Integer(4), "4"); hmCurr.put(new Integer(5), "5");
	 * hmCurr.put(new Integer(6), "6"); hmCurr.put(new Integer(7), "7");
	 */
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
			// Connect to topcon DB
			String topconURL = "jdbc:postgresql://localhost:5432/topcon";
			Connection topconCon = DriverManager.getConnection(topconURL, "jboss", "jboss");
			String empURL = "jdbc:postgresql://localhost:5432/wsemp";
			Connection empCon = DriverManager.getConnection(empURL, "jboss", "jboss");
			Statement topconStmt = topconCon.createStatement();
			Statement empStmt = empCon.createStatement();
			// Need the path to the input Stock Report
			String ARRptPath = req.getParameter("ARRptPath");
			if (ARRptPath != null && ARRptPath.trim().equals(""))
				throw new Exception("Invalid Stock Report Path / FileName");
			Log.printVerbose("Input file name = " + ARRptPath);
			// Input for date to generate the DN/CN
			String strTxDate = req.getParameter("txDate");
			Timestamp tsTxDate = TimeFormat.add(TimeFormat.createTimeStamp(strTxDate), 0, 0, 1);
			ARRptReader arRptReader = new ARRptReader(ARRptPath);
			String queryARBal = "select amount from acc_nominal_account where foreign_key = "
					+ "(select pkid from cust_account_index where acc_code = ? and currency='MYR')";
			PreparedStatement arBalStmt = empCon.prepareStatement(queryARBal);
			String queryAllARNonZero = "select ca.pkid,ca.acc_code,na.amount from acc_nominal_account na "
					+ "inner join cust_account_index ca on (ca.pkid=na.foreign_key) " + "where na.amount!=0";
			Log.printVerbose("*** TOPCON -> EMP ***");
			// Forward search, i.e given topcon's AR, search EMP's
			int countBalDiff = 0;
			curTask = new Task("Fix AR(TOPCON->EMP)", arRptReader.ARTable.size());
			int count = 0;
			for (Enumeration e = arRptReader.ARTable.elements(); e.hasMoreElements();)
			{
				// if (count == 10) break; // for debugging purposes
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printDebug("Processing Row " + ++count);
				String remarks = "To account for migration discrepancies";
				ARRptReader.ARRow thisAR = (ARRptReader.ARRow) e.nextElement();
				arBalStmt.setString(1, thisAR.custCode);
				ResultSet rsARBal = arBalStmt.executeQuery();
				if (rsARBal.next())
				{
					BigDecimal bdARBal = rsARBal.getBigDecimal("amount");
					// BigDecimal actualBal = new BigDecimal(thisAR.amount);
					BigDecimal bdBalDiff = bdARBal.subtract(thisAR.amount);
					Log.printVerbose("Diff = " + bdBalDiff);
					if (bdBalDiff.signum() < 0)
					{
						countBalDiff++;
						Log.printDebug(thisAR.custCode + ": " + bdBalDiff);
						// Fetch the itemId from ItemBean
						CustAccount thisCustAcc = CustAccountNut.getObjectByCode(thisAR.custCode);
						if (thisCustAcc == null)
						{
							Log.printDebug("!!!! Cannot find CustAccount " + thisAR.custCode);
						} else
						{
							Log.printDebug("Adjusting AR " + thisAR.custCode);
							Long newDNId = createDebitNote(thisCustAcc.getPkid(),
									GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE, GLCodeBean.INTEREST_REVENUE, tsTxDate,
									"", new Long(0), "", remarks, TimeFormat.getTimestamp(), usrid, "MYR", bdBalDiff
											.abs());
						} // end if (thisCustAcc == null)
					} // end if (bdBalDiff.signum() < 0)
					else if (bdBalDiff.signum() > 0)
					{
						countBalDiff++;
						Log.printDebug(thisAR.custCode + ": " + bdBalDiff);
						// Fetch the itemId from ItemBean
						CustAccount thisCustAcc = CustAccountNut.getObjectByCode(thisAR.custCode);
						if (thisCustAcc == null)
						{
							Log.printDebug("!!!! Cannot find CustAccount " + thisAR.custCode);
						} else
						{
							Log.printDebug("Adjusting AR " + thisAR.custCode);
							Long newCNId = createCreditNote(thisCustAcc.getPkid(),
									GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE, GLCodeBean.CASH_DISCOUNT, tsTxDate, "",
									new Long(0), "", remarks, TimeFormat.getTimestamp(), usrid, "MYR", bdBalDiff.abs());
						}
					} // end if (bdBalDiff.signum() > 0)
				} else
				{
					Log.printDebug(thisAR.custCode + ": NOT FOUND IN DB");
				}
			} // end for
			Log.printVerbose("countBalDiff = " + countBalDiff);
			Log.printVerbose("*** EMP -> TOPCON ***");
			// Forward search, i.e given topcon's AR, search EMP's
			countBalDiff = 0;
			count = 0;
			ResultSet rsAllARBalNonZero = empStmt.executeQuery(queryAllARNonZero);
			curTask = new Task("Fix AR(EMP->TOPCON)", rsAllARBalNonZero.getFetchSize());
			while (rsAllARBalNonZero.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printVerbose("Processing Row " + ++count);
				String thisAccCode = rsAllARBalNonZero.getString("acc_code");
				if (arRptReader.ARTable.get(thisAccCode) != null)
				{
					Log.printVerbose("Skipping " + thisAccCode + " ...");
					continue;
				}
				Integer thisARId = new Integer(rsAllARBalNonZero.getInt("pkid"));
				BigDecimal thisARAmt = rsAllARBalNonZero.getBigDecimal("amount");
				String remarks = "To account for migration discrepancies";
				countBalDiff++;
				Log.printVerbose("Adjusting AR " + thisAccCode + ": " + thisARAmt);
				if (thisARAmt.signum() < 0)
				{
					Long newDNId = createDebitNote(thisARId, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
							GLCodeBean.INTEREST_REVENUE, tsTxDate, "", new Long(0), "", remarks, TimeFormat
									.getTimestamp(), usrid, "MYR", thisARAmt.abs());
				} // end if (thisARAmt.signum() < 0)
				else if (thisARAmt.signum() > 0)
				{
					Long newCNId = createCreditNote(thisARId, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
							GLCodeBean.CASH_DISCOUNT, tsTxDate, "", new Long(0), "", remarks,
							TimeFormat.getTimestamp(), usrid, "MYR", thisARAmt.abs());
				} // end if (thisARAmt.signum() > 0)
				else
				{
					Log.printDebug("ERRRRRRRRRRRRRROOOOOOOOORRRRRRR!!!");
				}
			} // end while
			Log.printVerbose("countBalDiff = " + countBalDiff);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while fixing stock bal: " + ex.getMessage());
		}
		Log.printVerbose("***** END: FIX AR AMOUNT *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "FIX AR AMOUNT");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		// Clean up curTask;
		curTask = null;
		return new ActionRouter("test-migrate-topcondb-page");
	}

	private Long createCreditNote(
	// Integer naPkid, String stmtType,
			Integer custId, String stmtType, String glCodeDebit, Timestamp stmtDate, String fStmtTable, Long fStmtKey,
			String refNo, String remarks, Timestamp timeUpdate, Integer userId, String currency, BigDecimal amount)
			throws Exception
	{
		// try
		// {
		String strErrMsg = null;
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
			naObj.remarks = remarks;
			naObj.accPCCenterId = iDefPCCenterId;
			naObj.state = NominalAccountBean.STATE_CREATED;
			naObj.status = NominalAccountBean.STATUS_ACTIVE;
			naObj.lastUpdate = TimeFormat.getTimestamp();
			naObj.userIdUpdate = userId;
			NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
		}
		// / checking input variables
		// if(naPkid == null)
		// throw new Exception(" Invalid Nominal Account PKID ");
		// NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
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
		gsObj.remarks = remarks;
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

	private Long createDebitNote(
	// Integer naPkid, String stmtType,
			Integer custId, String stmtType, String glCodeCredit, Timestamp stmtDate, String fStmtTable, Long fStmtKey,
			String refNo, String remarks, Timestamp timeUpdate, Integer userId, String currency, BigDecimal amount)
			throws Exception
	{
		String strErrMsg = null;
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
			naObj.remarks = remarks;
			naObj.accPCCenterId = iDefPCCenterId;
			naObj.state = NominalAccountBean.STATE_CREATED;
			naObj.status = NominalAccountBean.STATUS_ACTIVE;
			naObj.lastUpdate = TimeFormat.getTimestamp();
			naObj.userIdUpdate = userId;
			NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
		}
		// / checking input variables
		// if(naPkid == null)
		// throw new Exception(" Invalid Nominal Account PKID ");
		// NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
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
		gsObj.remarks = remarks;
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
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;

//import com.vlee.ejb.inventory.*;
/**
 * 
 * DoTopConSyncAR ================= There are discrepancies between the migrated
 * Nominal Account Balances and TOPCON's excel AR report, since the Topcon DB
 * contains missing/duplicated/erroneous data To sync up the migrated data with
 * that of Topcon's "trusted" balances, the best we could do, since we have the
 * AR Outstanding Balances from TOPCON (assumed to be CORRECT !!), is to
 * programmatically insert either a DN/CN to offset the discrepancies so that
 * they be equal.
 * 
 */
public class DoTopConSyncAR implements Action
{
	private String strClassName = "DoTopConSyncAR";
	private static Task curTask = null;
	/***************************************************************************
	 * GLOBAL VARIABLES
	 **************************************************************************/
	Integer iDefLocId = new Integer(1000);
	Integer iDefSvcCtrId = new Integer(1);
	Integer iDefPCCenterId = new Integer(1000);
	Integer iDefCashAccId = new Integer(1000);
	String strDefCurr = "MYR";

	/*
	 * HashMap hmCurr = new HashMap(); hmCurr.put(new Integer(1), "MYR");
	 * hmCurr.put(new Integer(2), "USD"); hmCurr.put(new Integer(3), "SGD");
	 * hmCurr.put(new Integer(4), "4"); hmCurr.put(new Integer(5), "5");
	 * hmCurr.put(new Integer(6), "6"); hmCurr.put(new Integer(7), "7");
	 */
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
			// Connect to topcon DB
			String topconURL = "jdbc:postgresql://localhost:5432/topcon";
			Connection topconCon = DriverManager.getConnection(topconURL, "jboss", "jboss");
			String empURL = "jdbc:postgresql://localhost:5432/wsemp";
			Connection empCon = DriverManager.getConnection(empURL, "jboss", "jboss");
			Statement topconStmt = topconCon.createStatement();
			Statement empStmt = empCon.createStatement();
			// Need the path to the input Stock Report
			String ARRptPath = req.getParameter("ARRptPath");
			if (ARRptPath != null && ARRptPath.trim().equals(""))
				throw new Exception("Invalid Stock Report Path / FileName");
			Log.printVerbose("Input file name = " + ARRptPath);
			// Input for date to generate the DN/CN
			String strTxDate = req.getParameter("txDate");
			Timestamp tsTxDate = TimeFormat.add(TimeFormat.createTimeStamp(strTxDate), 0, 0, 1);
			ARRptReader arRptReader = new ARRptReader(ARRptPath);
			String queryARBal = "select amount from acc_nominal_account where foreign_key = "
					+ "(select pkid from cust_account_index where acc_code = ? and currency='MYR')";
			PreparedStatement arBalStmt = empCon.prepareStatement(queryARBal);
			String queryAllARNonZero = "select ca.pkid,ca.acc_code,na.amount from acc_nominal_account na "
					+ "inner join cust_account_index ca on (ca.pkid=na.foreign_key) " + "where na.amount!=0";
			Log.printVerbose("*** TOPCON -> EMP ***");
			// Forward search, i.e given topcon's AR, search EMP's
			int countBalDiff = 0;
			curTask = new Task("Fix AR(TOPCON->EMP)", arRptReader.ARTable.size());
			int count = 0;
			for (Enumeration e = arRptReader.ARTable.elements(); e.hasMoreElements();)
			{
				// if (count == 10) break; // for debugging purposes
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printDebug("Processing Row " + ++count);
				String remarks = "To account for migration discrepancies";
				ARRptReader.ARRow thisAR = (ARRptReader.ARRow) e.nextElement();
				arBalStmt.setString(1, thisAR.custCode);
				ResultSet rsARBal = arBalStmt.executeQuery();
				if (rsARBal.next())
				{
					BigDecimal bdARBal = rsARBal.getBigDecimal("amount");
					// BigDecimal actualBal = new BigDecimal(thisAR.amount);
					BigDecimal bdBalDiff = bdARBal.subtract(thisAR.amount);
					Log.printVerbose("Diff = " + bdBalDiff);
					if (bdBalDiff.signum() < 0)
					{
						countBalDiff++;
						Log.printDebug(thisAR.custCode + ": " + bdBalDiff);
						// Fetch the itemId from ItemBean
						CustAccount thisCustAcc = CustAccountNut.getObjectByCode(thisAR.custCode);
						if (thisCustAcc == null)
						{
							Log.printDebug("!!!! Cannot find CustAccount " + thisAR.custCode);
						} else
						{
							Log.printDebug("Adjusting AR " + thisAR.custCode);
							Long newDNId = createDebitNote(thisCustAcc.getPkid(),
									GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE, GLCodeBean.INTEREST_REVENUE, tsTxDate,
									"", new Long(0), "", remarks, TimeFormat.getTimestamp(), usrid, "MYR", bdBalDiff
											.abs());
						} // end if (thisCustAcc == null)
					} // end if (bdBalDiff.signum() < 0)
					else if (bdBalDiff.signum() > 0)
					{
						countBalDiff++;
						Log.printDebug(thisAR.custCode + ": " + bdBalDiff);
						// Fetch the itemId from ItemBean
						CustAccount thisCustAcc = CustAccountNut.getObjectByCode(thisAR.custCode);
						if (thisCustAcc == null)
						{
							Log.printDebug("!!!! Cannot find CustAccount " + thisAR.custCode);
						} else
						{
							Log.printDebug("Adjusting AR " + thisAR.custCode);
							Long newCNId = createCreditNote(thisCustAcc.getPkid(),
									GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE, GLCodeBean.CASH_DISCOUNT, tsTxDate, "",
									new Long(0), "", remarks, TimeFormat.getTimestamp(), usrid, "MYR", bdBalDiff.abs());
						}
					} // end if (bdBalDiff.signum() > 0)
				} else
				{
					Log.printDebug(thisAR.custCode + ": NOT FOUND IN DB");
				}
			} // end for
			Log.printVerbose("countBalDiff = " + countBalDiff);
			Log.printVerbose("*** EMP -> TOPCON ***");
			// Forward search, i.e given topcon's AR, search EMP's
			countBalDiff = 0;
			count = 0;
			ResultSet rsAllARBalNonZero = empStmt.executeQuery(queryAllARNonZero);
			curTask = new Task("Fix AR(EMP->TOPCON)", rsAllARBalNonZero.getFetchSize());
			while (rsAllARBalNonZero.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printVerbose("Processing Row " + ++count);
				String thisAccCode = rsAllARBalNonZero.getString("acc_code");
				if (arRptReader.ARTable.get(thisAccCode) != null)
				{
					Log.printVerbose("Skipping " + thisAccCode + " ...");
					continue;
				}
				Integer thisARId = new Integer(rsAllARBalNonZero.getInt("pkid"));
				BigDecimal thisARAmt = rsAllARBalNonZero.getBigDecimal("amount");
				String remarks = "To account for migration discrepancies";
				countBalDiff++;
				Log.printVerbose("Adjusting AR " + thisAccCode + ": " + thisARAmt);
				if (thisARAmt.signum() < 0)
				{
					Long newDNId = createDebitNote(thisARId, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
							GLCodeBean.INTEREST_REVENUE, tsTxDate, "", new Long(0), "", remarks, TimeFormat
									.getTimestamp(), usrid, "MYR", thisARAmt.abs());
				} // end if (thisARAmt.signum() < 0)
				else if (thisARAmt.signum() > 0)
				{
					Long newCNId = createCreditNote(thisARId, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
							GLCodeBean.CASH_DISCOUNT, tsTxDate, "", new Long(0), "", remarks,
							TimeFormat.getTimestamp(), usrid, "MYR", thisARAmt.abs());
				} // end if (thisARAmt.signum() > 0)
				else
				{
					Log.printDebug("ERRRRRRRRRRRRRROOOOOOOOORRRRRRR!!!");
				}
			} // end while
			Log.printVerbose("countBalDiff = " + countBalDiff);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while fixing stock bal: " + ex.getMessage());
		}
		Log.printVerbose("***** END: FIX AR AMOUNT *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "FIX AR AMOUNT");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		// Clean up curTask;
		curTask = null;
		return new ActionRouter("test-migrate-topcondb-page");
	}

	private Long createCreditNote(
	// Integer naPkid, String stmtType,
			Integer custId, String stmtType, String glCodeDebit, Timestamp stmtDate, String fStmtTable, Long fStmtKey,
			String refNo, String remarks, Timestamp timeUpdate, Integer userId, String currency, BigDecimal amount)
			throws Exception
	{
		// try
		// {
		String strErrMsg = null;
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
			naObj.remarks = remarks;
			naObj.accPCCenterId = iDefPCCenterId;
			naObj.state = NominalAccountBean.STATE_CREATED;
			naObj.status = NominalAccountBean.STATUS_ACTIVE;
			naObj.lastUpdate = TimeFormat.getTimestamp();
			naObj.userIdUpdate = userId;
			NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
		}
		// / checking input variables
		// if(naPkid == null)
		// throw new Exception(" Invalid Nominal Account PKID ");
		// NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
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
		gsObj.remarks = remarks;
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

	private Long createDebitNote(
	// Integer naPkid, String stmtType,
			Integer custId, String stmtType, String glCodeCredit, Timestamp stmtDate, String fStmtTable, Long fStmtKey,
			String refNo, String remarks, Timestamp timeUpdate, Integer userId, String currency, BigDecimal amount)
			throws Exception
	{
		String strErrMsg = null;
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
			naObj.remarks = remarks;
			naObj.accPCCenterId = iDefPCCenterId;
			naObj.state = NominalAccountBean.STATE_CREATED;
			naObj.status = NominalAccountBean.STATUS_ACTIVE;
			naObj.lastUpdate = TimeFormat.getTimestamp();
			naObj.userIdUpdate = userId;
			NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
		}
		// / checking input variables
		// if(naPkid == null)
		// throw new Exception(" Invalid Nominal Account PKID ");
		// NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
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
		gsObj.remarks = remarks;
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
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;

//import com.vlee.ejb.inventory.*;
/**
 * 
 * DoTopConSyncAR ================= There are discrepancies between the migrated
 * Nominal Account Balances and TOPCON's excel AR report, since the Topcon DB
 * contains missing/duplicated/erroneous data To sync up the migrated data with
 * that of Topcon's "trusted" balances, the best we could do, since we have the
 * AR Outstanding Balances from TOPCON (assumed to be CORRECT !!), is to
 * programmatically insert either a DN/CN to offset the discrepancies so that
 * they be equal.
 * 
 */
public class DoTopConSyncAR implements Action
{
	private String strClassName = "DoTopConSyncAR";
	private static Task curTask = null;
	/***************************************************************************
	 * GLOBAL VARIABLES
	 **************************************************************************/
	Integer iDefLocId = new Integer(1000);
	Integer iDefSvcCtrId = new Integer(1);
	Integer iDefPCCenterId = new Integer(1000);
	Integer iDefCashAccId = new Integer(1000);
	String strDefCurr = "MYR";

	/*
	 * HashMap hmCurr = new HashMap(); hmCurr.put(new Integer(1), "MYR");
	 * hmCurr.put(new Integer(2), "USD"); hmCurr.put(new Integer(3), "SGD");
	 * hmCurr.put(new Integer(4), "4"); hmCurr.put(new Integer(5), "5");
	 * hmCurr.put(new Integer(6), "6"); hmCurr.put(new Integer(7), "7");
	 */
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
			// Connect to topcon DB
			String topconURL = "jdbc:postgresql://localhost:5432/topcon";
			Connection topconCon = DriverManager.getConnection(topconURL, "jboss", "jboss");
			String empURL = "jdbc:postgresql://localhost:5432/wsemp";
			Connection empCon = DriverManager.getConnection(empURL, "jboss", "jboss");
			Statement topconStmt = topconCon.createStatement();
			Statement empStmt = empCon.createStatement();
			// Need the path to the input Stock Report
			String ARRptPath = req.getParameter("ARRptPath");
			if (ARRptPath != null && ARRptPath.trim().equals(""))
				throw new Exception("Invalid Stock Report Path / FileName");
			Log.printVerbose("Input file name = " + ARRptPath);
			// Input for date to generate the DN/CN
			String strTxDate = req.getParameter("txDate");
			Timestamp tsTxDate = TimeFormat.add(TimeFormat.createTimeStamp(strTxDate), 0, 0, 1);
			ARRptReader arRptReader = new ARRptReader(ARRptPath);
			String queryARBal = "select amount from acc_nominal_account where foreign_key = "
					+ "(select pkid from cust_account_index where acc_code = ? and currency='MYR')";
			PreparedStatement arBalStmt = empCon.prepareStatement(queryARBal);
			String queryAllARNonZero = "select ca.pkid,ca.acc_code,na.amount from acc_nominal_account na "
					+ "inner join cust_account_index ca on (ca.pkid=na.foreign_key) " + "where na.amount!=0";
			Log.printVerbose("*** TOPCON -> EMP ***");
			// Forward search, i.e given topcon's AR, search EMP's
			int countBalDiff = 0;
			curTask = new Task("Fix AR(TOPCON->EMP)", arRptReader.ARTable.size());
			int count = 0;
			for (Enumeration e = arRptReader.ARTable.elements(); e.hasMoreElements();)
			{
				// if (count == 10) break; // for debugging purposes
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printDebug("Processing Row " + ++count);
				String remarks = "To account for migration discrepancies";
				ARRptReader.ARRow thisAR = (ARRptReader.ARRow) e.nextElement();
				arBalStmt.setString(1, thisAR.custCode);
				ResultSet rsARBal = arBalStmt.executeQuery();
				if (rsARBal.next())
				{
					BigDecimal bdARBal = rsARBal.getBigDecimal("amount");
					// BigDecimal actualBal = new BigDecimal(thisAR.amount);
					BigDecimal bdBalDiff = bdARBal.subtract(thisAR.amount);
					Log.printVerbose("Diff = " + bdBalDiff);
					if (bdBalDiff.signum() < 0)
					{
						countBalDiff++;
						Log.printDebug(thisAR.custCode + ": " + bdBalDiff);
						// Fetch the itemId from ItemBean
						CustAccount thisCustAcc = CustAccountNut.getObjectByCode(thisAR.custCode);
						if (thisCustAcc == null)
						{
							Log.printDebug("!!!! Cannot find CustAccount " + thisAR.custCode);
						} else
						{
							Log.printDebug("Adjusting AR " + thisAR.custCode);
							Long newDNId = createDebitNote(thisCustAcc.getPkid(),
									GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE, GLCodeBean.INTEREST_REVENUE, tsTxDate,
									"", new Long(0), "", remarks, TimeFormat.getTimestamp(), usrid, "MYR", bdBalDiff
											.abs());
						} // end if (thisCustAcc == null)
					} // end if (bdBalDiff.signum() < 0)
					else if (bdBalDiff.signum() > 0)
					{
						countBalDiff++;
						Log.printDebug(thisAR.custCode + ": " + bdBalDiff);
						// Fetch the itemId from ItemBean
						CustAccount thisCustAcc = CustAccountNut.getObjectByCode(thisAR.custCode);
						if (thisCustAcc == null)
						{
							Log.printDebug("!!!! Cannot find CustAccount " + thisAR.custCode);
						} else
						{
							Log.printDebug("Adjusting AR " + thisAR.custCode);
							Long newCNId = createCreditNote(thisCustAcc.getPkid(),
									GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE, GLCodeBean.CASH_DISCOUNT, tsTxDate, "",
									new Long(0), "", remarks, TimeFormat.getTimestamp(), usrid, "MYR", bdBalDiff.abs());
						}
					} // end if (bdBalDiff.signum() > 0)
				} else
				{
					Log.printDebug(thisAR.custCode + ": NOT FOUND IN DB");
				}
			} // end for
			Log.printVerbose("countBalDiff = " + countBalDiff);
			Log.printVerbose("*** EMP -> TOPCON ***");
			// Forward search, i.e given topcon's AR, search EMP's
			countBalDiff = 0;
			count = 0;
			ResultSet rsAllARBalNonZero = empStmt.executeQuery(queryAllARNonZero);
			curTask = new Task("Fix AR(EMP->TOPCON)", rsAllARBalNonZero.getFetchSize());
			while (rsAllARBalNonZero.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printVerbose("Processing Row " + ++count);
				String thisAccCode = rsAllARBalNonZero.getString("acc_code");
				if (arRptReader.ARTable.get(thisAccCode) != null)
				{
					Log.printVerbose("Skipping " + thisAccCode + " ...");
					continue;
				}
				Integer thisARId = new Integer(rsAllARBalNonZero.getInt("pkid"));
				BigDecimal thisARAmt = rsAllARBalNonZero.getBigDecimal("amount");
				String remarks = "To account for migration discrepancies";
				countBalDiff++;
				Log.printVerbose("Adjusting AR " + thisAccCode + ": " + thisARAmt);
				if (thisARAmt.signum() < 0)
				{
					Long newDNId = createDebitNote(thisARId, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
							GLCodeBean.INTEREST_REVENUE, tsTxDate, "", new Long(0), "", remarks, TimeFormat
									.getTimestamp(), usrid, "MYR", thisARAmt.abs());
				} // end if (thisARAmt.signum() < 0)
				else if (thisARAmt.signum() > 0)
				{
					Long newCNId = createCreditNote(thisARId, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
							GLCodeBean.CASH_DISCOUNT, tsTxDate, "", new Long(0), "", remarks,
							TimeFormat.getTimestamp(), usrid, "MYR", thisARAmt.abs());
				} // end if (thisARAmt.signum() > 0)
				else
				{
					Log.printDebug("ERRRRRRRRRRRRRROOOOOOOOORRRRRRR!!!");
				}
			} // end while
			Log.printVerbose("countBalDiff = " + countBalDiff);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while fixing stock bal: " + ex.getMessage());
		}
		Log.printVerbose("***** END: FIX AR AMOUNT *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "FIX AR AMOUNT");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		// Clean up curTask;
		curTask = null;
		return new ActionRouter("test-migrate-topcondb-page");
	}

	private Long createCreditNote(
	// Integer naPkid, String stmtType,
			Integer custId, String stmtType, String glCodeDebit, Timestamp stmtDate, String fStmtTable, Long fStmtKey,
			String refNo, String remarks, Timestamp timeUpdate, Integer userId, String currency, BigDecimal amount)
			throws Exception
	{
		// try
		// {
		String strErrMsg = null;
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
			naObj.remarks = remarks;
			naObj.accPCCenterId = iDefPCCenterId;
			naObj.state = NominalAccountBean.STATE_CREATED;
			naObj.status = NominalAccountBean.STATUS_ACTIVE;
			naObj.lastUpdate = TimeFormat.getTimestamp();
			naObj.userIdUpdate = userId;
			NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
		}
		// / checking input variables
		// if(naPkid == null)
		// throw new Exception(" Invalid Nominal Account PKID ");
		// NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
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
		gsObj.remarks = remarks;
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

	private Long createDebitNote(
	// Integer naPkid, String stmtType,
			Integer custId, String stmtType, String glCodeCredit, Timestamp stmtDate, String fStmtTable, Long fStmtKey,
			String refNo, String remarks, Timestamp timeUpdate, Integer userId, String currency, BigDecimal amount)
			throws Exception
	{
		String strErrMsg = null;
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
			naObj.remarks = remarks;
			naObj.accPCCenterId = iDefPCCenterId;
			naObj.state = NominalAccountBean.STATE_CREATED;
			naObj.status = NominalAccountBean.STATUS_ACTIVE;
			naObj.lastUpdate = TimeFormat.getTimestamp();
			naObj.userIdUpdate = userId;
			NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
		}
		// / checking input variables
		// if(naPkid == null)
		// throw new Exception(" Invalid Nominal Account PKID ");
		// NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
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
		gsObj.remarks = remarks;
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
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;

//import com.vlee.ejb.inventory.*;
/**
 * 
 * DoTopConSyncAR ================= There are discrepancies between the migrated
 * Nominal Account Balances and TOPCON's excel AR report, since the Topcon DB
 * contains missing/duplicated/erroneous data To sync up the migrated data with
 * that of Topcon's "trusted" balances, the best we could do, since we have the
 * AR Outstanding Balances from TOPCON (assumed to be CORRECT !!), is to
 * programmatically insert either a DN/CN to offset the discrepancies so that
 * they be equal.
 * 
 */
public class DoTopConSyncAR implements Action
{
	private String strClassName = "DoTopConSyncAR";
	private static Task curTask = null;
	/***************************************************************************
	 * GLOBAL VARIABLES
	 **************************************************************************/
	Integer iDefLocId = new Integer(1000);
	Integer iDefSvcCtrId = new Integer(1);
	Integer iDefPCCenterId = new Integer(1000);
	Integer iDefCashAccId = new Integer(1000);
	String strDefCurr = "MYR";

	/*
	 * HashMap hmCurr = new HashMap(); hmCurr.put(new Integer(1), "MYR");
	 * hmCurr.put(new Integer(2), "USD"); hmCurr.put(new Integer(3), "SGD");
	 * hmCurr.put(new Integer(4), "4"); hmCurr.put(new Integer(5), "5");
	 * hmCurr.put(new Integer(6), "6"); hmCurr.put(new Integer(7), "7");
	 */
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
			// Connect to topcon DB
			String topconURL = "jdbc:postgresql://localhost:5432/topcon";
			Connection topconCon = DriverManager.getConnection(topconURL, "jboss", "jboss");
			String empURL = "jdbc:postgresql://localhost:5432/wsemp";
			Connection empCon = DriverManager.getConnection(empURL, "jboss", "jboss");
			Statement topconStmt = topconCon.createStatement();
			Statement empStmt = empCon.createStatement();
			// Need the path to the input Stock Report
			String ARRptPath = req.getParameter("ARRptPath");
			if (ARRptPath != null && ARRptPath.trim().equals(""))
				throw new Exception("Invalid Stock Report Path / FileName");
			Log.printVerbose("Input file name = " + ARRptPath);
			// Input for date to generate the DN/CN
			String strTxDate = req.getParameter("txDate");
			Timestamp tsTxDate = TimeFormat.add(TimeFormat.createTimeStamp(strTxDate), 0, 0, 1);
			ARRptReader arRptReader = new ARRptReader(ARRptPath);
			String queryARBal = "select amount from acc_nominal_account where foreign_key = "
					+ "(select pkid from cust_account_index where acc_code = ? and currency='MYR')";
			PreparedStatement arBalStmt = empCon.prepareStatement(queryARBal);
			String queryAllARNonZero = "select ca.pkid,ca.acc_code,na.amount from acc_nominal_account na "
					+ "inner join cust_account_index ca on (ca.pkid=na.foreign_key) " + "where na.amount!=0";
			Log.printVerbose("*** TOPCON -> EMP ***");
			// Forward search, i.e given topcon's AR, search EMP's
			int countBalDiff = 0;
			curTask = new Task("Fix AR(TOPCON->EMP)", arRptReader.ARTable.size());
			int count = 0;
			for (Enumeration e = arRptReader.ARTable.elements(); e.hasMoreElements();)
			{
				// if (count == 10) break; // for debugging purposes
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printDebug("Processing Row " + ++count);
				String remarks = "To account for migration discrepancies";
				ARRptReader.ARRow thisAR = (ARRptReader.ARRow) e.nextElement();
				arBalStmt.setString(1, thisAR.custCode);
				ResultSet rsARBal = arBalStmt.executeQuery();
				if (rsARBal.next())
				{
					BigDecimal bdARBal = rsARBal.getBigDecimal("amount");
					// BigDecimal actualBal = new BigDecimal(thisAR.amount);
					BigDecimal bdBalDiff = bdARBal.subtract(thisAR.amount);
					Log.printVerbose("Diff = " + bdBalDiff);
					if (bdBalDiff.signum() < 0)
					{
						countBalDiff++;
						Log.printDebug(thisAR.custCode + ": " + bdBalDiff);
						// Fetch the itemId from ItemBean
						CustAccount thisCustAcc = CustAccountNut.getObjectByCode(thisAR.custCode);
						if (thisCustAcc == null)
						{
							Log.printDebug("!!!! Cannot find CustAccount " + thisAR.custCode);
						} else
						{
							Log.printDebug("Adjusting AR " + thisAR.custCode);
							Long newDNId = createDebitNote(thisCustAcc.getPkid(),
									GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE, GLCodeBean.INTEREST_REVENUE, tsTxDate,
									"", new Long(0), "", remarks, TimeFormat.getTimestamp(), usrid, "MYR", bdBalDiff
											.abs());
						} // end if (thisCustAcc == null)
					} // end if (bdBalDiff.signum() < 0)
					else if (bdBalDiff.signum() > 0)
					{
						countBalDiff++;
						Log.printDebug(thisAR.custCode + ": " + bdBalDiff);
						// Fetch the itemId from ItemBean
						CustAccount thisCustAcc = CustAccountNut.getObjectByCode(thisAR.custCode);
						if (thisCustAcc == null)
						{
							Log.printDebug("!!!! Cannot find CustAccount " + thisAR.custCode);
						} else
						{
							Log.printDebug("Adjusting AR " + thisAR.custCode);
							Long newCNId = createCreditNote(thisCustAcc.getPkid(),
									GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE, GLCodeBean.CASH_DISCOUNT, tsTxDate, "",
									new Long(0), "", remarks, TimeFormat.getTimestamp(), usrid, "MYR", bdBalDiff.abs());
						}
					} // end if (bdBalDiff.signum() > 0)
				} else
				{
					Log.printDebug(thisAR.custCode + ": NOT FOUND IN DB");
				}
			} // end for
			Log.printVerbose("countBalDiff = " + countBalDiff);
			Log.printVerbose("*** EMP -> TOPCON ***");
			// Forward search, i.e given topcon's AR, search EMP's
			countBalDiff = 0;
			count = 0;
			ResultSet rsAllARBalNonZero = empStmt.executeQuery(queryAllARNonZero);
			curTask = new Task("Fix AR(EMP->TOPCON)", rsAllARBalNonZero.getFetchSize());
			while (rsAllARBalNonZero.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printVerbose("Processing Row " + ++count);
				String thisAccCode = rsAllARBalNonZero.getString("acc_code");
				if (arRptReader.ARTable.get(thisAccCode) != null)
				{
					Log.printVerbose("Skipping " + thisAccCode + " ...");
					continue;
				}
				Integer thisARId = new Integer(rsAllARBalNonZero.getInt("pkid"));
				BigDecimal thisARAmt = rsAllARBalNonZero.getBigDecimal("amount");
				String remarks = "To account for migration discrepancies";
				countBalDiff++;
				Log.printVerbose("Adjusting AR " + thisAccCode + ": " + thisARAmt);
				if (thisARAmt.signum() < 0)
				{
					Long newDNId = createDebitNote(thisARId, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
							GLCodeBean.INTEREST_REVENUE, tsTxDate, "", new Long(0), "", remarks, TimeFormat
									.getTimestamp(), usrid, "MYR", thisARAmt.abs());
				} // end if (thisARAmt.signum() < 0)
				else if (thisARAmt.signum() > 0)
				{
					Long newCNId = createCreditNote(thisARId, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
							GLCodeBean.CASH_DISCOUNT, tsTxDate, "", new Long(0), "", remarks,
							TimeFormat.getTimestamp(), usrid, "MYR", thisARAmt.abs());
				} // end if (thisARAmt.signum() > 0)
				else
				{
					Log.printDebug("ERRRRRRRRRRRRRROOOOOOOOORRRRRRR!!!");
				}
			} // end while
			Log.printVerbose("countBalDiff = " + countBalDiff);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while fixing stock bal: " + ex.getMessage());
		}
		Log.printVerbose("***** END: FIX AR AMOUNT *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "FIX AR AMOUNT");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		// Clean up curTask;
		curTask = null;
		return new ActionRouter("test-migrate-topcondb-page");
	}

	private Long createCreditNote(
	// Integer naPkid, String stmtType,
			Integer custId, String stmtType, String glCodeDebit, Timestamp stmtDate, String fStmtTable, Long fStmtKey,
			String refNo, String remarks, Timestamp timeUpdate, Integer userId, String currency, BigDecimal amount)
			throws Exception
	{
		// try
		// {
		String strErrMsg = null;
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
			naObj.remarks = remarks;
			naObj.accPCCenterId = iDefPCCenterId;
			naObj.state = NominalAccountBean.STATE_CREATED;
			naObj.status = NominalAccountBean.STATUS_ACTIVE;
			naObj.lastUpdate = TimeFormat.getTimestamp();
			naObj.userIdUpdate = userId;
			NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
		}
		// / checking input variables
		// if(naPkid == null)
		// throw new Exception(" Invalid Nominal Account PKID ");
		// NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
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
		gsObj.remarks = remarks;
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

	private Long createDebitNote(
	// Integer naPkid, String stmtType,
			Integer custId, String stmtType, String glCodeCredit, Timestamp stmtDate, String fStmtTable, Long fStmtKey,
			String refNo, String remarks, Timestamp timeUpdate, Integer userId, String currency, BigDecimal amount)
			throws Exception
	{
		String strErrMsg = null;
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
			naObj.remarks = remarks;
			naObj.accPCCenterId = iDefPCCenterId;
			naObj.state = NominalAccountBean.STATE_CREATED;
			naObj.status = NominalAccountBean.STATUS_ACTIVE;
			naObj.lastUpdate = TimeFormat.getTimestamp();
			naObj.userIdUpdate = userId;
			NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
		}
		// / checking input variables
		// if(naPkid == null)
		// throw new Exception(" Invalid Nominal Account PKID ");
		// NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
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
		gsObj.remarks = remarks;
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
}
