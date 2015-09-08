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
 * 
 * This fix is used for: [1] Issuing of DN / CN to offset exch rate gain/loss if
 * gain, either customers pay more, offset with DN, else CN
 */
public class DoTopConFixExRate implements Action
{
	private String strClassName = "DoTopConFixExRate";
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
			// Statement topconStmt = con.createStatement();
			// Statement jbossStmt = jbossCon.createStatement();
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
			String findInvQ =
			// "select pkid from cust_invoice_index where remarks ~* ? ";
			"select pkid from cust_invoice_index where remarks ILIKE ? ";
			PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
			String findDNQ = "select pkid from acc_generic_stmt where remarks ILIKE ? ";
			PreparedStatement findDNStmt = jbossCon.prepareStatement(findDNQ);
			String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
			PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
			String findCurrQ = "select currid from cdhis where docno = ? ";
			PreparedStatement findCurrStmt = con.prepareStatement(findCurrQ);
			String queryXRateGainLose = "select * from payrefd where gainloseamt != 0 ";
			Statement xRateGainLoseStmt = con.createStatement();
			ResultSet rsXRateGainLose = xRateGainLoseStmt.executeQuery(queryXRateGainLose);
			int count = 0;
			while (rsXRateGainLose.next())
			{
				Log.printVerbose("*** Processing Txn " + ++count);
				String strPayNo = rsXRateGainLose.getString("payno");
				String strTxRefNo = rsXRateGainLose.getString("txrefno");
				String strTxType = rsXRateGainLose.getString("txtype");
				String strCustCode = rsXRateGainLose.getString("custid");
				Timestamp payDate = TimeFormat.createTimeStamp(rsXRateGainLose.getString("paydate"),
						"MM/dd/yy HH:mm:ss");
				BigDecimal bdPayExchRate = rsXRateGainLose.getBigDecimal("exch_rate");
				BigDecimal bdTxExchRate = rsXRateGainLose.getBigDecimal("txexch_rate");
				BigDecimal bdGainLoseAmt = rsXRateGainLose.getBigDecimal("gainloseamt");
				// Get the Invoice Id or DN Id
				Long refDocId = new Long(0);
				String refDocTable = "";
				if (strTxType.equals("I"))
				{
					findInvStmt.setString(1, "% = " + strTxRefNo + ")%");
					ResultSet rsFindInv = findInvStmt.executeQuery();
					if (rsFindInv.next())
					{
						refDocId = new Long(rsFindInv.getLong("pkid"));
						refDocTable = InvoiceBean.TABLENAME;
					}
				} else
				{
					findDNStmt.setString(1, "% = " + strTxRefNo + ")%");
					ResultSet rsFindDN = findDNStmt.executeQuery();
					if (rsFindDN.next())
					{
						refDocId = new Long(rsFindDN.getLong("pkid"));
						refDocTable = GenericStmtBean.TABLENAME;
					}
				}
				// Get the Customer Id
				Integer custId = null;
				findCustStmt.setString(1, strCustCode);
				
				Log.printVerbose("findCustStmt: "+findCurrStmt.toString());
				
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
				String strRemarks = "EXCH_RATE OFFSET (txexch_rate=" + bdTxExchRate.toString() + ", pay_exch_rate="
						+ bdPayExchRate.toString() + ")";
				// if bdGainLoseAmt < 0, means we need to CN to offset the
				// balance
				// if bdGainLoseAmt > 0, means we need to DN to offset the
				// balance
				Long newGenStmtId = new Long(0);
				if (bdGainLoseAmt.signum() < 0)
				{
					Log.printVerbose("*** " + count + ": Creating CN");
					newGenStmtId = createCreditNote(custId, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
							GLCodeBean.CASH_DISCOUNT, payDate, refDocTable, refDocId, strPayNo, strRemarks, TimeFormat
									.getTimestamp(),
							// usrid, strCurr, bdGainLoseAmt.abs());
							usrid, strDefCurr, bdGainLoseAmt.abs());
				} else
				{
					// Create a Debit Note
					Log.printVerbose("*** " + count + ": Creating DN");
					newGenStmtId = createDebitNote(custId, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
							GLCodeBean.INTEREST_REVENUE, payDate, refDocTable, refDocId, strPayNo, strRemarks,
							TimeFormat.getTimestamp(),
							// usrid, strCurr, bdGainLoseAmt.abs());
							usrid, strDefCurr, bdGainLoseAmt.abs());
				}
				/*
				 * // TMP fix here String getGenId = "select pkid from
				 * acc_generic_stmt where remarks ~* 'EXCH_RATE OFFSET' and
				 * foreign_stmt_table = '" + refDocTable + "' and
				 * foreign_stmt_key = " + refDocId.toString();
				 * Log.printVerbose(getGenId); Statement genIdStmt =
				 * jbossCon.createStatement(); ResultSet rsGenId =
				 * genIdStmt.executeQuery(getGenId); if(rsGenId.next()) {
				 * newGenStmtId = new Long(rsGenId.getLong("pkid")); }
				 */
				// Now create the necessary DocLink to balance the invoices
				DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_ADJ,
						GenericStmtBean.TABLENAME, newGenStmtId, refDocTable, refDocId, strDefCurr, bdGainLoseAmt, "",
						new BigDecimal(0), "", TimeFormat.getTimestamp(), usrid);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while fixing NAT: " + ex.getMessage());
		}
		Log.printVerbose("***** END: FIX EXCH RATE *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "FIX EXCH RATE");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
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
		
		Log.printVerbose("gsObj.namespace"+gsObj.namespace);
		Log.printVerbose("gsObj.dateStmt"+gsObj.dateStmt.toString());
		Log.printVerbose("gsObj.glCodeDebit"+gsObj.glCodeDebit);
		Log.printVerbose("gsObj.glCodeCredit"+gsObj.glCodeCredit);
		Log.printVerbose("gsObj.currency"+gsObj.currency);
		Log.printVerbose("gsObj.amount"+gsObj.amount.toString());
		Log.printVerbose("gsObj.dateDue"+gsObj.dateDue.toString());
		Log.printVerbose("gsObj.foreignEntityTable"+gsObj.foreignEntityTable);
		Log.printVerbose("gsObj.foreignEntityKey"+gsObj.foreignEntityKey.toString());
		Log.printVerbose("gsObj.pcCenter"+gsObj.pcCenter.toString());
		Log.printVerbose("gsObj.stmtType"+gsObj.stmtType);
		Log.printVerbose("gsObj.foreignStmtTable"+gsObj.foreignStmtTable);
		Log.printVerbose("gsObj.foreignStmtKey"+gsObj.foreignStmtKey.toString());
		Log.printVerbose("gsObj.referenceNo"+gsObj.referenceNo);
		Log.printVerbose("gsObj.remarks"+gsObj.remarks);
		Log.printVerbose("gsObj.dateStart"+gsObj.dateStart.toString());
		Log.printVerbose("gsObj.dateEnd"+gsObj.dateEnd.toString());
		Log.printVerbose("gsObj.dateDue"+gsObj.dateDue.toString());
		Log.printVerbose("gsObj.dateCreated"+gsObj.dateCreated.toString());
		Log.printVerbose("gsObj.dateApproved"+gsObj.dateApproved.toString());
		Log.printVerbose("gsObj.dateVerified"+gsObj.dateVerified.toString());
		Log.printVerbose("gsObj.dateUpdate"+gsObj.dateUpdate.toString());
		Log.printVerbose("gsObj.userIdCreate"+gsObj.userIdCreate.toString());
		Log.printVerbose("gsObj.userIdPIC"+gsObj.userIdPIC.toString());
		Log.printVerbose("gsObj.userIdApprove"+gsObj.userIdApprove.toString());
		Log.printVerbose("gsObj.userIdVerified"+gsObj.userIdVerified.toString());
		Log.printVerbose("gsObj.userIdUpdate"+gsObj.userIdUpdate.toString());
		Log.printVerbose("natObj.nominalAccount"+natObj.nominalAccount.toString());
		Log.printVerbose("natObj.foreignTable"+natObj.foreignTable);
		Log.printVerbose("natObj.description"+natObj.description);
		Log.printVerbose("natObj.glCodeDebit"+natObj.glCodeDebit);
		Log.printVerbose("natObj.glCodeCredit"+natObj.glCodeCredit);
		Log.printVerbose("natObj.currency"+natObj.currency);
		Log.printVerbose("natObj.amount"+natObj.amount.toString());
		Log.printVerbose("natObj.timeOption1"+natObj.timeOption1);
		Log.printVerbose("natObj.timeParam1"+natObj.timeParam1.toString());
		Log.printVerbose("natObj.state"+natObj.state);
		Log.printVerbose("natObj.status"+natObj.status);
		Log.printVerbose("natObj.lastUpdate"+natObj.lastUpdate.toString());
		Log.printVerbose("natObj.userIdUpdate"+natObj.userIdUpdate.toString());
		
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
 * 
 * This fix is used for: [1] Issuing of DN / CN to offset exch rate gain/loss if
 * gain, either customers pay more, offset with DN, else CN
 */
public class DoTopConFixExRate implements Action
{
	private String strClassName = "DoTopConFixExRate";
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
			// Statement topconStmt = con.createStatement();
			// Statement jbossStmt = jbossCon.createStatement();
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
			String findInvQ =
			// "select pkid from cust_invoice_index where remarks ~* ? ";
			"select pkid from cust_invoice_index where remarks ILIKE ? ";
			PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
			String findDNQ = "select pkid from acc_generic_stmt where remarks ILIKE ? ";
			PreparedStatement findDNStmt = jbossCon.prepareStatement(findDNQ);
			String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
			PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
			String findCurrQ = "select currid from cdhis where docno = ? ";
			PreparedStatement findCurrStmt = con.prepareStatement(findCurrQ);
			String queryXRateGainLose = "select * from payrefd where gainloseamt != 0 ";
			Statement xRateGainLoseStmt = con.createStatement();
			ResultSet rsXRateGainLose = xRateGainLoseStmt.executeQuery(queryXRateGainLose);
			int count = 0;
			while (rsXRateGainLose.next())
			{
				Log.printVerbose("*** Processing Txn " + ++count);
				String strPayNo = rsXRateGainLose.getString("payno");
				String strTxRefNo = rsXRateGainLose.getString("txrefno");
				String strTxType = rsXRateGainLose.getString("txtype");
				String strCustCode = rsXRateGainLose.getString("custid");
				Timestamp payDate = TimeFormat.createTimeStamp(rsXRateGainLose.getString("paydate"),
						"MM/dd/yy HH:mm:ss");
				BigDecimal bdPayExchRate = rsXRateGainLose.getBigDecimal("exch_rate");
				BigDecimal bdTxExchRate = rsXRateGainLose.getBigDecimal("txexch_rate");
				BigDecimal bdGainLoseAmt = rsXRateGainLose.getBigDecimal("gainloseamt");
				// Get the Invoice Id or DN Id
				Long refDocId = new Long(0);
				String refDocTable = "";
				if (strTxType.equals("I"))
				{
					findInvStmt.setString(1, "% = " + strTxRefNo + ")%");
					ResultSet rsFindInv = findInvStmt.executeQuery();
					if (rsFindInv.next())
					{
						refDocId = new Long(rsFindInv.getLong("pkid"));
						refDocTable = InvoiceBean.TABLENAME;
					}
				} else
				{
					findDNStmt.setString(1, "% = " + strTxRefNo + ")%");
					ResultSet rsFindDN = findDNStmt.executeQuery();
					if (rsFindDN.next())
					{
						refDocId = new Long(rsFindDN.getLong("pkid"));
						refDocTable = GenericStmtBean.TABLENAME;
					}
				}
				// Get the Customer Id
				Integer custId = null;
				findCustStmt.setString(1, strCustCode);
				
				Log.printVerbose("findCustStmt: "+findCurrStmt.toString());
				
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
				String strRemarks = "EXCH_RATE OFFSET (txexch_rate=" + bdTxExchRate.toString() + ", pay_exch_rate="
						+ bdPayExchRate.toString() + ")";
				// if bdGainLoseAmt < 0, means we need to CN to offset the
				// balance
				// if bdGainLoseAmt > 0, means we need to DN to offset the
				// balance
				Long newGenStmtId = new Long(0);
				if (bdGainLoseAmt.signum() < 0)
				{
					Log.printVerbose("*** " + count + ": Creating CN");
					newGenStmtId = createCreditNote(custId, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
							GLCodeBean.CASH_DISCOUNT, payDate, refDocTable, refDocId, strPayNo, strRemarks, TimeFormat
									.getTimestamp(),
							// usrid, strCurr, bdGainLoseAmt.abs());
							usrid, strDefCurr, bdGainLoseAmt.abs());
				} else
				{
					// Create a Debit Note
					Log.printVerbose("*** " + count + ": Creating DN");
					newGenStmtId = createDebitNote(custId, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
							GLCodeBean.INTEREST_REVENUE, payDate, refDocTable, refDocId, strPayNo, strRemarks,
							TimeFormat.getTimestamp(),
							// usrid, strCurr, bdGainLoseAmt.abs());
							usrid, strDefCurr, bdGainLoseAmt.abs());
				}
				/*
				 * // TMP fix here String getGenId = "select pkid from
				 * acc_generic_stmt where remarks ~* 'EXCH_RATE OFFSET' and
				 * foreign_stmt_table = '" + refDocTable + "' and
				 * foreign_stmt_key = " + refDocId.toString();
				 * Log.printVerbose(getGenId); Statement genIdStmt =
				 * jbossCon.createStatement(); ResultSet rsGenId =
				 * genIdStmt.executeQuery(getGenId); if(rsGenId.next()) {
				 * newGenStmtId = new Long(rsGenId.getLong("pkid")); }
				 */
				// Now create the necessary DocLink to balance the invoices
				DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_ADJ,
						GenericStmtBean.TABLENAME, newGenStmtId, refDocTable, refDocId, strDefCurr, bdGainLoseAmt, "",
						new BigDecimal(0), "", TimeFormat.getTimestamp(), usrid);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while fixing NAT: " + ex.getMessage());
		}
		Log.printVerbose("***** END: FIX EXCH RATE *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "FIX EXCH RATE");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
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
		
		Log.printVerbose("gsObj.namespace"+gsObj.namespace);
		Log.printVerbose("gsObj.dateStmt"+gsObj.dateStmt.toString());
		Log.printVerbose("gsObj.glCodeDebit"+gsObj.glCodeDebit);
		Log.printVerbose("gsObj.glCodeCredit"+gsObj.glCodeCredit);
		Log.printVerbose("gsObj.currency"+gsObj.currency);
		Log.printVerbose("gsObj.amount"+gsObj.amount.toString());
		Log.printVerbose("gsObj.dateDue"+gsObj.dateDue.toString());
		Log.printVerbose("gsObj.foreignEntityTable"+gsObj.foreignEntityTable);
		Log.printVerbose("gsObj.foreignEntityKey"+gsObj.foreignEntityKey.toString());
		Log.printVerbose("gsObj.pcCenter"+gsObj.pcCenter.toString());
		Log.printVerbose("gsObj.stmtType"+gsObj.stmtType);
		Log.printVerbose("gsObj.foreignStmtTable"+gsObj.foreignStmtTable);
		Log.printVerbose("gsObj.foreignStmtKey"+gsObj.foreignStmtKey.toString());
		Log.printVerbose("gsObj.referenceNo"+gsObj.referenceNo);
		Log.printVerbose("gsObj.remarks"+gsObj.remarks);
		Log.printVerbose("gsObj.dateStart"+gsObj.dateStart.toString());
		Log.printVerbose("gsObj.dateEnd"+gsObj.dateEnd.toString());
		Log.printVerbose("gsObj.dateDue"+gsObj.dateDue.toString());
		Log.printVerbose("gsObj.dateCreated"+gsObj.dateCreated.toString());
		Log.printVerbose("gsObj.dateApproved"+gsObj.dateApproved.toString());
		Log.printVerbose("gsObj.dateVerified"+gsObj.dateVerified.toString());
		Log.printVerbose("gsObj.dateUpdate"+gsObj.dateUpdate.toString());
		Log.printVerbose("gsObj.userIdCreate"+gsObj.userIdCreate.toString());
		Log.printVerbose("gsObj.userIdPIC"+gsObj.userIdPIC.toString());
		Log.printVerbose("gsObj.userIdApprove"+gsObj.userIdApprove.toString());
		Log.printVerbose("gsObj.userIdVerified"+gsObj.userIdVerified.toString());
		Log.printVerbose("gsObj.userIdUpdate"+gsObj.userIdUpdate.toString());
		Log.printVerbose("natObj.nominalAccount"+natObj.nominalAccount.toString());
		Log.printVerbose("natObj.foreignTable"+natObj.foreignTable);
		Log.printVerbose("natObj.description"+natObj.description);
		Log.printVerbose("natObj.glCodeDebit"+natObj.glCodeDebit);
		Log.printVerbose("natObj.glCodeCredit"+natObj.glCodeCredit);
		Log.printVerbose("natObj.currency"+natObj.currency);
		Log.printVerbose("natObj.amount"+natObj.amount.toString());
		Log.printVerbose("natObj.timeOption1"+natObj.timeOption1);
		Log.printVerbose("natObj.timeParam1"+natObj.timeParam1.toString());
		Log.printVerbose("natObj.state"+natObj.state);
		Log.printVerbose("natObj.status"+natObj.status);
		Log.printVerbose("natObj.lastUpdate"+natObj.lastUpdate.toString());
		Log.printVerbose("natObj.userIdUpdate"+natObj.userIdUpdate.toString());
		
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
 * 
 * This fix is used for: [1] Issuing of DN / CN to offset exch rate gain/loss if
 * gain, either customers pay more, offset with DN, else CN
 */
public class DoTopConFixExRate implements Action
{
	private String strClassName = "DoTopConFixExRate";
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
			// Statement topconStmt = con.createStatement();
			// Statement jbossStmt = jbossCon.createStatement();
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
			String findInvQ =
			// "select pkid from cust_invoice_index where remarks ~* ? ";
			"select pkid from cust_invoice_index where remarks ILIKE ? ";
			PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
			String findDNQ = "select pkid from acc_generic_stmt where remarks ILIKE ? ";
			PreparedStatement findDNStmt = jbossCon.prepareStatement(findDNQ);
			String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
			PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
			String findCurrQ = "select currid from cdhis where docno = ? ";
			PreparedStatement findCurrStmt = con.prepareStatement(findCurrQ);
			String queryXRateGainLose = "select * from payrefd where gainloseamt != 0 ";
			Statement xRateGainLoseStmt = con.createStatement();
			ResultSet rsXRateGainLose = xRateGainLoseStmt.executeQuery(queryXRateGainLose);
			int count = 0;
			while (rsXRateGainLose.next())
			{
				Log.printVerbose("*** Processing Txn " + ++count);
				String strPayNo = rsXRateGainLose.getString("payno");
				String strTxRefNo = rsXRateGainLose.getString("txrefno");
				String strTxType = rsXRateGainLose.getString("txtype");
				String strCustCode = rsXRateGainLose.getString("custid");
				Timestamp payDate = TimeFormat.createTimeStamp(rsXRateGainLose.getString("paydate"),
						"MM/dd/yy HH:mm:ss");
				BigDecimal bdPayExchRate = rsXRateGainLose.getBigDecimal("exch_rate");
				BigDecimal bdTxExchRate = rsXRateGainLose.getBigDecimal("txexch_rate");
				BigDecimal bdGainLoseAmt = rsXRateGainLose.getBigDecimal("gainloseamt");
				// Get the Invoice Id or DN Id
				Long refDocId = new Long(0);
				String refDocTable = "";
				if (strTxType.equals("I"))
				{
					findInvStmt.setString(1, "% = " + strTxRefNo + ")%");
					ResultSet rsFindInv = findInvStmt.executeQuery();
					if (rsFindInv.next())
					{
						refDocId = new Long(rsFindInv.getLong("pkid"));
						refDocTable = InvoiceBean.TABLENAME;
					}
				} else
				{
					findDNStmt.setString(1, "% = " + strTxRefNo + ")%");
					ResultSet rsFindDN = findDNStmt.executeQuery();
					if (rsFindDN.next())
					{
						refDocId = new Long(rsFindDN.getLong("pkid"));
						refDocTable = GenericStmtBean.TABLENAME;
					}
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
				String strRemarks = "EXCH_RATE OFFSET (txexch_rate=" + bdTxExchRate.toString() + ", pay_exch_rate="
						+ bdPayExchRate.toString() + ")";
				// if bdGainLoseAmt < 0, means we need to CN to offset the
				// balance
				// if bdGainLoseAmt > 0, means we need to DN to offset the
				// balance
				Long newGenStmtId = new Long(0);
				if (bdGainLoseAmt.signum() < 0)
				{
					Log.printVerbose("*** " + count + ": Creating CN");
					newGenStmtId = createCreditNote(custId, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
							GLCodeBean.CASH_DISCOUNT, payDate, refDocTable, refDocId, strPayNo, strRemarks, TimeFormat
									.getTimestamp(),
							// usrid, strCurr, bdGainLoseAmt.abs());
							usrid, strDefCurr, bdGainLoseAmt.abs());
				} else
				{
					// Create a Debit Note
					Log.printVerbose("*** " + count + ": Creating DN");
					newGenStmtId = createDebitNote(custId, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
							GLCodeBean.INTEREST_REVENUE, payDate, refDocTable, refDocId, strPayNo, strRemarks,
							TimeFormat.getTimestamp(),
							// usrid, strCurr, bdGainLoseAmt.abs());
							usrid, strDefCurr, bdGainLoseAmt.abs());
				}
				/*
				 * // TMP fix here String getGenId = "select pkid from
				 * acc_generic_stmt where remarks ~* 'EXCH_RATE OFFSET' and
				 * foreign_stmt_table = '" + refDocTable + "' and
				 * foreign_stmt_key = " + refDocId.toString();
				 * Log.printVerbose(getGenId); Statement genIdStmt =
				 * jbossCon.createStatement(); ResultSet rsGenId =
				 * genIdStmt.executeQuery(getGenId); if(rsGenId.next()) {
				 * newGenStmtId = new Long(rsGenId.getLong("pkid")); }
				 */
				// Now create the necessary DocLink to balance the invoices
				DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_ADJ,
						GenericStmtBean.TABLENAME, newGenStmtId, refDocTable, refDocId, strDefCurr, bdGainLoseAmt, "",
						new BigDecimal(0), "", TimeFormat.getTimestamp(), usrid);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while fixing NAT: " + ex.getMessage());
		}
		Log.printVerbose("***** END: FIX EXCH RATE *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "FIX EXCH RATE");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
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
 * 
 * This fix is used for: [1] Issuing of DN / CN to offset exch rate gain/loss if
 * gain, either customers pay more, offset with DN, else CN
 */
public class DoTopConFixExRate implements Action
{
	private String strClassName = "DoTopConFixExRate";
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
			// Statement topconStmt = con.createStatement();
			// Statement jbossStmt = jbossCon.createStatement();
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
			String findInvQ =
			// "select pkid from cust_invoice_index where remarks ~* ? ";
			"select pkid from cust_invoice_index where remarks ILIKE ? ";
			PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
			String findDNQ = "select pkid from acc_generic_stmt where remarks ILIKE ? ";
			PreparedStatement findDNStmt = jbossCon.prepareStatement(findDNQ);
			String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
			PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
			String findCurrQ = "select currid from cdhis where docno = ? ";
			PreparedStatement findCurrStmt = con.prepareStatement(findCurrQ);
			String queryXRateGainLose = "select * from payrefd where gainloseamt != 0 ";
			Statement xRateGainLoseStmt = con.createStatement();
			ResultSet rsXRateGainLose = xRateGainLoseStmt.executeQuery(queryXRateGainLose);
			int count = 0;
			while (rsXRateGainLose.next())
			{
				Log.printVerbose("*** Processing Txn " + ++count);
				String strPayNo = rsXRateGainLose.getString("payno");
				String strTxRefNo = rsXRateGainLose.getString("txrefno");
				String strTxType = rsXRateGainLose.getString("txtype");
				String strCustCode = rsXRateGainLose.getString("custid");
				Timestamp payDate = TimeFormat.createTimeStamp(rsXRateGainLose.getString("paydate"),
						"MM/dd/yy HH:mm:ss");
				BigDecimal bdPayExchRate = rsXRateGainLose.getBigDecimal("exch_rate");
				BigDecimal bdTxExchRate = rsXRateGainLose.getBigDecimal("txexch_rate");
				BigDecimal bdGainLoseAmt = rsXRateGainLose.getBigDecimal("gainloseamt");
				// Get the Invoice Id or DN Id
				Long refDocId = new Long(0);
				String refDocTable = "";
				if (strTxType.equals("I"))
				{
					findInvStmt.setString(1, "% = " + strTxRefNo + ")%");
					ResultSet rsFindInv = findInvStmt.executeQuery();
					if (rsFindInv.next())
					{
						refDocId = new Long(rsFindInv.getLong("pkid"));
						refDocTable = InvoiceBean.TABLENAME;
					}
				} else
				{
					findDNStmt.setString(1, "% = " + strTxRefNo + ")%");
					ResultSet rsFindDN = findDNStmt.executeQuery();
					if (rsFindDN.next())
					{
						refDocId = new Long(rsFindDN.getLong("pkid"));
						refDocTable = GenericStmtBean.TABLENAME;
					}
				}
				// Get the Customer Id
				Integer custId = null;
				findCustStmt.setString(1, strCustCode);
				
				Log.printVerbose("findCustStmt: "+findCurrStmt.toString());
				
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
				String strRemarks = "EXCH_RATE OFFSET (txexch_rate=" + bdTxExchRate.toString() + ", pay_exch_rate="
						+ bdPayExchRate.toString() + ")";
				// if bdGainLoseAmt < 0, means we need to CN to offset the
				// balance
				// if bdGainLoseAmt > 0, means we need to DN to offset the
				// balance
				Long newGenStmtId = new Long(0);
				if (bdGainLoseAmt.signum() < 0)
				{
					Log.printVerbose("*** " + count + ": Creating CN");
					newGenStmtId = createCreditNote(custId, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE,
							GLCodeBean.CASH_DISCOUNT, payDate, refDocTable, refDocId, strPayNo, strRemarks, TimeFormat
									.getTimestamp(),
							// usrid, strCurr, bdGainLoseAmt.abs());
							usrid, strDefCurr, bdGainLoseAmt.abs());
				} else
				{
					// Create a Debit Note
					Log.printVerbose("*** " + count + ": Creating DN");
					newGenStmtId = createDebitNote(custId, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
							GLCodeBean.INTEREST_REVENUE, payDate, refDocTable, refDocId, strPayNo, strRemarks,
							TimeFormat.getTimestamp(),
							// usrid, strCurr, bdGainLoseAmt.abs());
							usrid, strDefCurr, bdGainLoseAmt.abs());
				}
				/*
				 * // TMP fix here String getGenId = "select pkid from
				 * acc_generic_stmt where remarks ~* 'EXCH_RATE OFFSET' and
				 * foreign_stmt_table = '" + refDocTable + "' and
				 * foreign_stmt_key = " + refDocId.toString();
				 * Log.printVerbose(getGenId); Statement genIdStmt =
				 * jbossCon.createStatement(); ResultSet rsGenId =
				 * genIdStmt.executeQuery(getGenId); if(rsGenId.next()) {
				 * newGenStmtId = new Long(rsGenId.getLong("pkid")); }
				 */
				// Now create the necessary DocLink to balance the invoices
				DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_ADJ,
						GenericStmtBean.TABLENAME, newGenStmtId, refDocTable, refDocId, strDefCurr, bdGainLoseAmt, "",
						new BigDecimal(0), "", TimeFormat.getTimestamp(), usrid);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while fixing NAT: " + ex.getMessage());
		}
		Log.printVerbose("***** END: FIX EXCH RATE *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "FIX EXCH RATE");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
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
		
		Log.printVerbose("gsObj.namespace"+gsObj.namespace);
		Log.printVerbose("gsObj.dateStmt"+gsObj.dateStmt.toString());
		Log.printVerbose("gsObj.glCodeDebit"+gsObj.glCodeDebit);
		Log.printVerbose("gsObj.glCodeCredit"+gsObj.glCodeCredit);
		Log.printVerbose("gsObj.currency"+gsObj.currency);
		Log.printVerbose("gsObj.amount"+gsObj.amount.toString());
		Log.printVerbose("gsObj.dateDue"+gsObj.dateDue.toString());
		Log.printVerbose("gsObj.foreignEntityTable"+gsObj.foreignEntityTable);
		Log.printVerbose("gsObj.foreignEntityKey"+gsObj.foreignEntityKey.toString());
		Log.printVerbose("gsObj.pcCenter"+gsObj.pcCenter.toString());
		Log.printVerbose("gsObj.stmtType"+gsObj.stmtType);
		Log.printVerbose("gsObj.foreignStmtTable"+gsObj.foreignStmtTable);
		Log.printVerbose("gsObj.foreignStmtKey"+gsObj.foreignStmtKey.toString());
		Log.printVerbose("gsObj.referenceNo"+gsObj.referenceNo);
		Log.printVerbose("gsObj.remarks"+gsObj.remarks);
		Log.printVerbose("gsObj.dateStart"+gsObj.dateStart.toString());
		Log.printVerbose("gsObj.dateEnd"+gsObj.dateEnd.toString());
		Log.printVerbose("gsObj.dateDue"+gsObj.dateDue.toString());
		Log.printVerbose("gsObj.dateCreated"+gsObj.dateCreated.toString());
		Log.printVerbose("gsObj.dateApproved"+gsObj.dateApproved.toString());
		Log.printVerbose("gsObj.dateVerified"+gsObj.dateVerified.toString());
		Log.printVerbose("gsObj.dateUpdate"+gsObj.dateUpdate.toString());
		Log.printVerbose("gsObj.userIdCreate"+gsObj.userIdCreate.toString());
		Log.printVerbose("gsObj.userIdPIC"+gsObj.userIdPIC.toString());
		Log.printVerbose("gsObj.userIdApprove"+gsObj.userIdApprove.toString());
		Log.printVerbose("gsObj.userIdVerified"+gsObj.userIdVerified.toString());
		Log.printVerbose("gsObj.userIdUpdate"+gsObj.userIdUpdate.toString());
		Log.printVerbose("natObj.nominalAccount"+natObj.nominalAccount.toString());
		Log.printVerbose("natObj.foreignTable"+natObj.foreignTable);
		Log.printVerbose("natObj.description"+natObj.description);
		Log.printVerbose("natObj.glCodeDebit"+natObj.glCodeDebit);
		Log.printVerbose("natObj.glCodeCredit"+natObj.glCodeCredit);
		Log.printVerbose("natObj.currency"+natObj.currency);
		Log.printVerbose("natObj.amount"+natObj.amount.toString());
		Log.printVerbose("natObj.timeOption1"+natObj.timeOption1);
		Log.printVerbose("natObj.timeParam1"+natObj.timeParam1.toString());
		Log.printVerbose("natObj.state"+natObj.state);
		Log.printVerbose("natObj.status"+natObj.status);
		Log.printVerbose("natObj.lastUpdate"+natObj.lastUpdate.toString());
		Log.printVerbose("natObj.userIdUpdate"+natObj.userIdUpdate.toString());
		
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
}
