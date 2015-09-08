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
 (NEW) 
 Aug 04 DB seems to have solved a few of these discrepancies in the Jul 04 DB.

 CustID solved:
 MA0014
 ME0031
 ME0033 ?? need to issue DN to reverse OR20303
 MC0103 Change CN of 765 t0 716.5
 MK0015
 SS0080
 SW0004

 (NEW) SU0048
 Action: Issue CN to reduce amount for R/OR190104

 (NEW) MS0010
 The following Invoices / DNs were invalidated by topcon, hence has to be reversed:

 docno  | txtype | ldebitamt | lcreditamt | txstatus
 ---------+--------+-----------+------------+----------
 SM_476  | I      | 4450.0000 |     0.0000 | C
 SM_745  | I      | 2300.0000 |     0.0000 | C
 VM_1731 | I      |   92.0000 |     0.0000 | C
 VM_1756 | I      |  295.0000 |     0.0000 | C
 VM_1880 | I      |  105.0000 |     0.0000 | C
 DN1880  | D      |  105.0000 |     0.0000 | C


 **/
/**
 * These are the last few fixes ...
 *  * MA0014 (OR16506 both RP and P) Action: Issue Receipt to account for
 * OR16506
 *  * MC0014 CITY OPTICAL-KL 77834904 -21370.00 (MED410 oversettled SM634 +
 * RV11782 not in payrefd) Action: Issue CN to reverse RV11782 (1130.00)
 *  * MC0103 (O/R52473 is not found in payrefd for MC0103) Action: (1) Issue
 * Receipt of 48.00 to settle VM4626 (2) Issue CN of 765.00 to make up the
 * negative balance ????? dunno why !!
 *  * ME0031 (OR14097 both RP and P) Action: Issue CN to account for OR14097
 *  * ME0033 (O/R17437 not found in cdhis) Action: Issue Receipt O/R17437* for
 * SM1117
 *  * MK0015 (OR15621 unaccounted for) Action: Issue Receipt OR15621* for SM913
 *  * MS0010 (SM476,SM745,VM1731, VM1756,VM1880 overpaid, CN1880 Credited for
 * nothing (both C and I) Action: Issue DN to reverse CM1880
 *  * SM0040 (OR8814 overpayment for nonexistant RV8814 DN) Action: Issue DN to
 * reverse OR8814
 *  * SP0046 PRAI CONSTRN.& 04399 9006 -100.00 (O/R19126 unaccounted for - not
 * found in payrefd -exception!!) Action: Issue DN to reverse O/R19126
 *  * SS0080 (O/R17435 that pays for SS10258 is DELETED!!) Action: Issue Receipt
 * for SS10258 (1250.00)
 *  * SW0004 (OR12495 paid TWICE for SS1655) Action: Issue Receipt using
 * OR12495* to close SS1655 (payrefd has dup entries)
 * 
 */
public class DoTopConFixMisc implements Action
{
	private String strClassName = "DoTopConFixMisc";
	/***************************************************************************
	 * GLOBAL VARIABLES
	 **************************************************************************/
	Integer iDefLocId = new Integer(1000);
	Integer iDefSvcCtrId = new Integer(1);
	Integer iDefPCCenterId = new Integer(1000);
	Integer iDefCondId = new Integer(StockNut.STK_COND_GOOD);
	Integer iDefCashAccId = new Integer(1000);
	Integer iDefChequeAccId = new Integer(1001);
	String strDefCurr = "MYR";
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
	private BranchObject defBranchObj = null;
	Connection con = null;
	Connection jbossCon = null;

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Get processing start time
		Timestamp tsStart = TimeFormat.getTimestamp();
		if (defBranchObj == null)
			defBranchObj = BranchNut.getObject(iDefSvcCtrId);
		/*
		 * HashMap hmCurr = new HashMap(); hmCurr.put(new Integer(1), "MYR");
		 * hmCurr.put(new Integer(2), "USD"); hmCurr.put(new Integer(3), "SGD");
		 * hmCurr.put(new Integer(4), "4"); hmCurr.put(new Integer(5), "5");
		 * hmCurr.put(new Integer(6), "6"); hmCurr.put(new Integer(7), "7");
		 */
		try
		{
			// Connection to topcon DB
			String url = "jdbc:postgresql://localhost:5432/topcon";
			con = DriverManager.getConnection(url, "jboss", "jboss");
			// Connection to Wavelet EMP DB
			String jbossURL = "jdbc:postgresql://localhost:5432/wsemp";
			jbossCon = DriverManager.getConnection(jbossURL, "jboss", "jboss");
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
			/*
			 * String findInvQ = "select pkid from cust_invoice_index where
			 * remarks ~* ? "; PreparedStatement findInvStmt =
			 * jbossCon.prepareStatement(findInvQ);
			 * 
			 * String findCustQ = "select pkid from cust_account_index where
			 * acc_code = ? "; PreparedStatement findCustStmt =
			 * jbossCon.prepareStatement(findCustQ);
			 * 
			 * String findCurrQ = "select currid from cdhis where docno = ? ";
			 * PreparedStatement findCurrStmt = con.prepareStatement(findCurrQ);
			 * 
			 * String getDocInfo = "select * from cdhis where docno = ? ";
			 * PreparedStatement getDocInfoStmt =
			 * con.prepareStatement(getDocInfo);
			 */
			/*******************************************************************
			 * CustCode: MA0014
			 * 
			 * Fix: Issue Receipt (OR16506*) for SM1042*
			 ******************************************************************/
			// issueReceipt(con, jbossCon, "OR16506", "SM1042", usrid);
			/*******************************************************************
			 * CustCode: MC0014 Fix: Issue CN to reverse RV11782 (1130.00)
			 ******************************************************************/
			issueCN(con, jbossCon, "RV11782", usrid);
			/*******************************************************************
			 * CustCode: MC0103 Fix: (1) Issue Receipt O/R52473* for Inv. VM4626
			 * (2) Issue CN to make up negative balance (-765)
			 ******************************************************************/
			issueReceipt(con, jbossCon, "O/R52473", "VM4626", usrid);
			String getDate = "select txdate from cdhis where docno = 'OR_52473'";
			Statement getDateStmt = con.createStatement();
			ResultSet rsGetDate = getDateStmt.executeQuery(getDate);
			Timestamp txDate = TimeFormat.getTimestamp();
			if (rsGetDate.next())
			{
				txDate = rsGetDate.getTimestamp("txdate");
			}
			String strRemarks = "DERIVED: Unresolved negative balance";
			// issueNewCN(con,jbossCon,"MC0103",new BigDecimal(765.00),
			issueNewCN(con, jbossCon, "MC0103", new BigDecimal(716.50), txDate, usrid, strRemarks);
			/*******************************************************************
			 * CustCode: ME0031 Fix: Issue Receipt (OR14097*) for SM1064
			 ******************************************************************/
			// issueReceipt(con, jbossCon, "OR14097", "SM1064", usrid);
			/*******************************************************************
			 * CustCode: ME0033 Fix: Issue Receipt (O/R17437*) for SM1117
			 ******************************************************************/
			// issueNewReceipt(con, jbossCon, "ME0033", "O/R17437", "SM1117",
			// usrid);
			issueDN(con, jbossCon, "OR20303", usrid);
			/*******************************************************************
			 * CustCode: MK0015 Fix: Issue Receipt (OR15621*) for SM913
			 ******************************************************************/
			// issueReceipt(con, jbossCon, "OR15621", "SM913", usrid);
			/*******************************************************************
			 * CustCode: MS0010 Fix: Issue DN to reverse CM1880
			 ******************************************************************/
			issueDN(con, jbossCon, "CM1880", usrid);
			cancelInv("SM_476", usrid);
			cancelInv("SM_745", usrid);
			cancelInv("VM_1731", usrid);
			cancelInv("VM_1756", usrid);
			cancelInv("VM_1880", usrid);
			cancelGenStmt("DN1880");
			/*******************************************************************
			 * CustCode: SM0040 Fix: Issue DN/ReversePayment to reverse OR8814
			 * for non-existant DN RV8814
			 ******************************************************************/
			issueDN(con, jbossCon, "OR8814", usrid);
			/*******************************************************************
			 * CustCode: SP0046 Fix: Issue DN/ReversePayment to reverse O/R19126
			 ******************************************************************/
			issueDN(con, jbossCon, "O/R19126", usrid);
			/*******************************************************************
			 * CustCode: SS0080 Fix: Issue Receipt O/R17435 for Inv. SS10258
			 ******************************************************************/
			// issueReceipt(con, jbossCon, "O/R17435", "SS10258", usrid);
			/*******************************************************************
			 * CustCode: SU0048 Fix: Issue Receipt OR12495 for Inv. SS1655
			 ******************************************************************/
			getDate = "select txdate from cdhis where docno = 'R/OR190104'";
			rsGetDate = getDateStmt.executeQuery(getDate);
			txDate = TimeFormat.getTimestamp();
			if (rsGetDate.next())
			{
				txDate = rsGetDate.getTimestamp("txdate");
			}
			strRemarks = "DERIVED: Correct Reverse Payment R/OR190104";
			// issueNewCN(con,jbossCon,"MC0103",new BigDecimal(765.00),
			issueNewCN(con, jbossCon, "SU0048", new BigDecimal(500), txDate, usrid, strRemarks);
			/*******************************************************************
			 * CustCode: SW0004 Fix: Issue Receipt OR12495 for Inv. SS1655
			 ******************************************************************/
			// issueReceipt(con, jbossCon, "OR12495", "SS1655", usrid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while fixing NAT: " + ex.getMessage());
		}
		Log.printVerbose("***** END: FIX MISCELLANEOUS *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "FIX MISCELLANEOUS");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		return new ActionRouter("test-migrate-topcondb-page");
	}

	private void issueReceipt(Connection topconCon, Connection jbossCon, String payNo, String docRefNo, Integer usrid)
			throws Exception
	{
		String findInvQ =
		// "select pkid from cust_invoice_index where remarks ~* ? ";
		"select pkid from cust_invoice_index where remarks ILIKE ? ";
		PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		String getDocInfo = "select * from cdhis where docno = ? ";
		PreparedStatement getDocInfoStmt = topconCon.prepareStatement(getDocInfo);
		String strCustId = null;
		Integer iCustId = null;
		String strDocNo = null;
		String strCurr = null;
		String strRemarks = "DERIVED PAYMENT (Old DocRef = " + payNo + "**)";
		BigDecimal bdAmount = null;
		Timestamp tsTxDate = null;
		// Get the doc info
		getDocInfoStmt.setString(1, payNo);
		ResultSet rsGetDocInfo = getDocInfoStmt.executeQuery();
		if (rsGetDocInfo.next())
		{
			strCustId = rsGetDocInfo.getString("custid");
			strCurr = rsGetDocInfo.getString("currid");
			strCurr = (String) hmCurr.get(new Integer(strCurr));
			strDocNo = rsGetDocInfo.getString("docno");
			tsTxDate = rsGetDocInfo.getTimestamp("txdate");
			bdAmount = rsGetDocInfo.getBigDecimal("lcreditamt");
			// strRemarks = rsGetDocInfo.getString("comment");
		}
		getDocInfoStmt.close();
		Log.printDebug("##### Processing Cust " + strCustId + ": " + payNo + " #####");
		// Get the Customer Id
		findCustStmt.setString(1, strCustId);
		ResultSet rsFindCust = findCustStmt.executeQuery();
		if (rsFindCust.next())
		{
			iCustId = new Integer(rsFindCust.getInt("pkid"));
		}
		findCustStmt.close();
		// Query for Details from CDHisD
		String txnDQuery = "select desc1 from cdhisd where docref = '" + payNo + "'";
		Statement txnDStmt = topconCon.createStatement();
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
			strRemarks = strRemarks + " (DESC=" + desc1 + ")";
		}
		txnDStmt.close();
		Long newRcptId = createCustReceipt(iCustId, iCashAccId, tsTxDate, tsTxDate, tsTxDate, strCurr, bdAmount,
				strPayMethod, strRemarks, strChequeNo, // "" for Cash
				usrid, TimeFormat.getTimestamp());
		Log.printVerbose("Successfully created Receipt " + payNo);
		// Get the Invoice Id
		Long refDocId = new Long(0);
		findInvStmt.setString(1, "% = " + docRefNo + ")%");
		ResultSet rsFindInv = findInvStmt.executeQuery();
		if (rsFindInv.next())
		{
			refDocId = new Long(rsFindInv.getLong("pkid"));
		}
		// Create the doclink
		DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_ADJ,
		// ReceiptBean.TABLENAME, newRcptId,
				OfficialReceiptBean.TABLENAME, newRcptId, InvoiceBean.TABLENAME, refDocId, strCurr, bdAmount.negate(),
				"", bdAmount.negate(), "", TimeFormat.getTimestamp(), usrid);
	}

	private void issueNewReceipt(Connection topconCon, Connection jbossCon, String strCustId, String payNo,
			String docRefNo, Integer usrid) throws Exception
	{
		String findInvQ =
		// "select pkid from cust_invoice_index where remarks ~* ? ";
		"select pkid from cust_invoice_index where remarks ILIKE ? ";
		PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		Integer iCustId = null;
		Log.printDebug("##### Processing Cust " + strCustId + ": " + payNo + " #####");
		// Get the Customer Id
		findCustStmt.setString(1, strCustId);
		ResultSet rsFindCust = findCustStmt.executeQuery();
		if (rsFindCust.next())
		{
			iCustId = new Integer(rsFindCust.getInt("pkid"));
		}
		findCustStmt.close();
		/*
		 * // Query for Details from CDHisD String txnDQuery = "select desc1
		 * from cdhisd where docref = '" + payNo + "'"; Statement txnDStmt =
		 * con.createStatement(); ResultSet rsTxnD =
		 * txnDStmt.executeQuery(txnDQuery);
		 * 
		 * String strPayMethod = "cash"; // default to cash String strChequeNo =
		 * ""; Integer iCashAccId = iDefCashAccId; //while(rsTxnD.next())
		 * if(rsTxnD.next()) { //String glId = rsTxnD.getString("glid"); String
		 * desc1 = rsTxnD.getString("desc1").trim();
		 * 
		 * if (!desc1.equals("") && !desc1.startsWith("0/")) { strPayMethod =
		 * "cheque"; strChequeNo = desc1; iCashAccId = iDefChequeAccId; }
		 * //remarks = remarks + ", (GLID=" + glId strRemarks = strRemarks + "
		 * (DESC=" + desc1 + ")"; } txnDStmt.close();
		 */
		String getPaymentInfo = "select paydate,lpayamt from payrefd where payno = '" + payNo + "'";
		Statement getPaymentInfoStmt = topconCon.createStatement();
		ResultSet rsPymtInfo = getPaymentInfoStmt.executeQuery(getPaymentInfo);
		Timestamp tsTxDate = null;
		BigDecimal bdAmount = null;
		if (rsPymtInfo.next())
		{
			tsTxDate = TimeFormat.createTimeStamp(rsPymtInfo.getString("paydate"), "MM/dd/yy HH:mm:ss");
			bdAmount = rsPymtInfo.getBigDecimal("lpayamt");
		}
		String strRemarks = "DERIVED PAYMENT (Old DocRef = " + payNo + "**)";
		Long newRcptId = createCustReceipt(iCustId, iDefCashAccId, tsTxDate, tsTxDate, tsTxDate, "MYR", bdAmount,
				"cash", strRemarks, "", // "" for Cash
				usrid, TimeFormat.getTimestamp());
		Log.printVerbose("Successfully created Receipt " + payNo);
		// Get the Invoice Id
		Long refDocId = new Long(0);
		findInvStmt.setString(1, "% = " + docRefNo + ")%");
		ResultSet rsFindInv = findInvStmt.executeQuery();
		if (rsFindInv.next())
		{
			refDocId = new Long(rsFindInv.getLong("pkid"));
		}
		// Create the doclink
		DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_ADJ,
		// ReceiptBean.TABLENAME, newRcptId,
				OfficialReceiptBean.TABLENAME, newRcptId, InvoiceBean.TABLENAME, refDocId, "MYR", bdAmount.negate(),
				"", bdAmount.negate(), "", TimeFormat.getTimestamp(), usrid);
	}

	private void issueCN(Connection topconCon, Connection jbossCon, String docRefNo, Integer usrid) throws Exception
	{
		String findInvQ =
		// "select pkid from cust_invoice_index where remarks ~* ? ";
		"select pkid from cust_invoice_index where remarks ILIKE ? ";
		PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
		String findGenQ =
		// "select pkid from acc_generic_stmt where remarks ~* ? ";
		"select pkid from acc_generic_stmt where remarks ILIKE ? ";
		PreparedStatement findGenStmt = jbossCon.prepareStatement(findGenQ);
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		String getDocInfo = "select * from cdhis where docno = ? ";
		PreparedStatement getDocInfoStmt = topconCon.prepareStatement(getDocInfo);
		String strCustId = null;
		Integer iCustId = null;
		String strDocNo = null;
		String strCurr = null;
		String strTxType = null;
		String strRemarks = "DERIVED: To Reverse " + docRefNo;
		BigDecimal bdAmount = null;
		Timestamp tsTxDate = null;
		// Get the doc info
		getDocInfoStmt.setString(1, docRefNo);
		ResultSet rsGetDocInfo = getDocInfoStmt.executeQuery();
		if (rsGetDocInfo.next())
		{
			strCustId = rsGetDocInfo.getString("custid");
			strCurr = rsGetDocInfo.getString("currid");
			strCurr = (String) hmCurr.get(new Integer(strCurr));
			strDocNo = rsGetDocInfo.getString("docno");
			strTxType = rsGetDocInfo.getString("txtype");
			tsTxDate = rsGetDocInfo.getTimestamp("txdate");
			// bdAmount = rsGetDocInfo.getBigDecimal("lcreditamt");
			bdAmount = rsGetDocInfo.getBigDecimal("ldebitamt");
			// strRemarks = rsGetDocInfo.getString("comment");
		}
		getDocInfoStmt.close();
		Log.printDebug("##### Processing Cust " + strCustId + ": DocRef = " + docRefNo + " #####");
		// Get the Customer Id
		findCustStmt.setString(1, strCustId);
		ResultSet rsFindCust = findCustStmt.executeQuery();
		if (rsFindCust.next())
		{
			iCustId = new Integer(rsFindCust.getInt("pkid"));
		}
		findCustStmt.close();
		Long refDocId = new Long(0);
		String refDocTable = null;
		// Depends on what we are reversing, could be I, DN, or RP
		if (strTxType.equals("I"))
		{
			// Get the Invoice Id
			findInvStmt.setString(1, "% = " + docRefNo + ")%");
			ResultSet rsFindInv = findInvStmt.executeQuery();
			if (rsFindInv.next())
			{
				refDocId = new Long(rsFindInv.getLong("pkid"));
				refDocTable = InvoiceBean.TABLENAME;
			}
		} else
		{
			// Get the GenericStmt Id
			findGenStmt.setString(1, "% = " + docRefNo + ")%");
			ResultSet rsFindGenStmt = findGenStmt.executeQuery();
			if (rsFindGenStmt.next())
			{
				refDocId = new Long(rsFindGenStmt.getLong("pkid"));
				refDocTable = GenericStmtBean.TABLENAME;
			}
		}
		Long newCNId = createCreditNote(iCustId, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE, GLCodeBean.CASH_DISCOUNT,
				tsTxDate, refDocTable, refDocId, docRefNo, strRemarks, TimeFormat.getTimestamp(),
				// usrid, strCurr, bdAmount);
				usrid, strDefCurr, bdAmount);
		// Create the doclink
		DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_ADJ,
				GenericStmtBean.TABLENAME, newCNId, refDocTable, refDocId, strCurr, bdAmount.negate(), "",
				bdAmount.negate(), "", TimeFormat.getTimestamp(), usrid);
	}

	private void issueNewCN(Connection topconCon, Connection jbossCon, String strCustId, BigDecimal bdAmount,
			Timestamp txDate, Integer usrid, String remarks) throws Exception
	{
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		String strRemarks = "DERIVED: Unresolved negative balance";
		Log.printDebug("##### Processing Cust " + strCustId + ": NEW CREDIT NOTE");
		// Get the Customer Id
		Integer iCustId = null;
		findCustStmt.setString(1, strCustId);
		ResultSet rsFindCust = findCustStmt.executeQuery();
		if (rsFindCust.next())
		{
			iCustId = new Integer(rsFindCust.getInt("pkid"));
		}
		findCustStmt.close();
		Long newCNId = createCreditNote(iCustId, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE, GLCodeBean.CASH_DISCOUNT,
				txDate, "", new Long(0), "", strRemarks, TimeFormat.getTimestamp(), usrid, strDefCurr, bdAmount);
	}

	private void issueDN(Connection topconCon, Connection jbossCon, String docRefNo, Integer usrid) throws Exception
	{
		String findInvQ =
		// "select pkid from cust_receipt_index where payment_remarks ~* ? ";
		"select pkid from cust_receipt_index where payment_remarks ILIKE ? ";
		PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
		String findGenQ = "select pkid from acc_generic_stmt where remarks ILIKE ? ";
		PreparedStatement findGenStmt = jbossCon.prepareStatement(findGenQ);
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		String getDocInfo = "select * from cdhis where docno = ? ";
		PreparedStatement getDocInfoStmt = topconCon.prepareStatement(getDocInfo);
		String strCustId = null;
		Integer iCustId = null;
		String strDocNo = null;
		String strCurr = null;
		String strTxType = null;
		String strRemarks = "DERIVED: To Reverse " + docRefNo;
		BigDecimal bdAmount = null;
		Timestamp tsTxDate = null;
		// Get the doc info
		getDocInfoStmt.setString(1, docRefNo);
		ResultSet rsGetDocInfo = getDocInfoStmt.executeQuery();
		if (rsGetDocInfo.next())
		{
			strCustId = rsGetDocInfo.getString("custid");
			strCurr = rsGetDocInfo.getString("currid");
			strCurr = (String) hmCurr.get(new Integer(strCurr));
			strDocNo = rsGetDocInfo.getString("docno");
			strTxType = rsGetDocInfo.getString("txtype");
			tsTxDate = rsGetDocInfo.getTimestamp("txdate");
			bdAmount = rsGetDocInfo.getBigDecimal("lcreditamt");
			// strRemarks = rsGetDocInfo.getString("comment");
		}
		if (!strCurr.equals(strDefCurr))
		{
			strRemarks += ", (CURR=" + strCurr + ")";
		}
		getDocInfoStmt.close();
		Log.printDebug("##### Processing Cust " + strCustId + ": DocRef = " + docRefNo + " #####");
		// Get the Customer Id
		findCustStmt.setString(1, strCustId);
		ResultSet rsFindCust = findCustStmt.executeQuery();
		if (rsFindCust.next())
		{
			iCustId = new Integer(rsFindCust.getInt("pkid"));
		}
		findCustStmt.close();
		Long refDocId = new Long(0);
		String refDocTable = null;
		// Depends on what we are reversing, could be C or P
		if (strTxType.equals("P"))
		{
			// Get the Receipt Id
			findInvStmt.setString(1, "% = " + docRefNo + ")%");
			ResultSet rsFindInv = findInvStmt.executeQuery();
			if (rsFindInv.next())
			{
				refDocId = new Long(rsFindInv.getLong("pkid"));
				// refDocTable = ReceiptBean.TABLENAME;
				refDocTable = OfficialReceiptBean.TABLENAME;
			}
		} else
		{
			// Get the GenericStmt Id
			findGenStmt.setString(1, "% = " + docRefNo + ")%");
			ResultSet rsFindGenStmt = findGenStmt.executeQuery();
			if (rsFindGenStmt.next())
			{
				refDocId = new Long(rsFindGenStmt.getLong("pkid"));
				refDocTable = GenericStmtBean.TABLENAME;
			}
		}
		Long newDNId = createDebitNote(iCustId, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE, GLCodeBean.INTEREST_REVENUE,
				tsTxDate, refDocTable, refDocId, docRefNo, strRemarks, TimeFormat.getTimestamp(),
				// usrid, strCurr, bdAmount);
				usrid, strDefCurr, bdAmount);
		// Create the doclink
		DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_ADJ,
				GenericStmtBean.TABLENAME, newDNId, refDocTable, refDocId, strCurr, bdAmount, "", bdAmount, "",
				TimeFormat.getTimestamp(), usrid);
	}

	private Long createCustReceipt(Integer custId, // naPkid,
			Integer caPkid, Timestamp tsReceiptDate, Timestamp tsEffFromDate, Timestamp tsEffToDate, String currency,
			BigDecimal bdPaymentAmt, String strPaymentMethod, String strPaymentRmks,
			// String strGLCodeCredit,
			// String strGLCodeDebit,
			String strChequeNumber,
			// String strCashAccount,
			Integer iUsrId, Timestamp tsCreate) throws Exception
	{
		Long salesTxnId = new Long("0");
		String strAmountStr = CurrencyFormat.toWords(bdPaymentAmt, "", "");
		strAmountStr = strAmountStr + " ONLY";
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
			naObj.remarks = strPaymentRmks;
			naObj.accPCCenterId = iDefPCCenterId;
			naObj.state = NominalAccountBean.STATE_CREATED;
			naObj.status = NominalAccountBean.STATUS_ACTIVE;
			naObj.lastUpdate = TimeFormat.getTimestamp();
			naObj.userIdUpdate = iUsrId;
			NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
		}
		// / Get objects based on parameters above
		// NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
		CashAccountObject caObj = CashAccountNut.getObject(caPkid);
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
		/*
		 * NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		 * natObj.nominalAccount = naObj.pkid; natObj.foreignTable =
		 * NominalAccountTxnBean.FT_CUST_RECEIPT; natObj.foreignKey =
		 * cRcptObj.pkid; natObj.code = "not_used"; natObj.info1 = "";
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
		 * if(natEJB == null) { try { sPayEJB.remove();} catch(Exception ex) {
		 * ex.printStackTrace();} throw new Exception("Failed to create NAT"); }
		 * /// 4) update cash account transactions CashAccTxnObject catObj = new
		 * CashAccTxnObject(); catObj.txnCode = CashAccTxnBean.TXN_CODE_DEFAULT;
		 * catObj.glCodeCredit = GLCodeBean.ACC_RECEIVABLE; catObj.glCodeDebit =
		 * caObj.accountType; catObj.personInCharge = iUsrId; catObj.accFrom =
		 * new Integer("0");// another known bank account catObj.accTo =
		 * caObj.pkId; catObj.foreignTable = CashAccTxnBean.FT_CUST_RECEIPT;
		 * catObj.foreignKey = cRcptObj.pkid; catObj.currency = naObj.currency;
		 * catObj.amount = bdPaymentAmt; catObj.txnTime = tsReceiptDate;
		 * catObj.remarks = strPaymentRmks; catObj.info1 = ""; catObj.info2 =
		 * ""; catObj.state = CashAccTxnBean.ST_CREATED; catObj.status =
		 * CashAccTxnBean.STATUS_ACTIVE; catObj.lastUpdate = tsCreate;
		 * catObj.userIdUpdate = iUsrId; catObj.pcCenter = caObj.pcCenter;
		 * CashAccTxn catEJB = CashAccTxnNut.fnCreate(catObj); /// 5) update
		 * cash account balance // no need to set the balance !!! hurray !!!!
		 * 
		 * /// 3) update the nominal account NominalAccount naEJB =
		 * NominalAccountNut.getHandle(naObj.pkid); BigDecimal bdLatestBal =
		 * naEJB.getAmount(); bdLatestBal = bdLatestBal.add(natObj.amount);
		 * naObj.amount = bdLatestBal;
		 */
		// return sPayEJB.getPkid();
		return receiptObj.pkid;
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

	private Long createReversePayment(String strStmtType,
	// Integer iNominalAccId,
			Integer iCustId, Integer iCashAccountId, String currency, BigDecimal bdPaymentAmt,
			String strChequeCreditCardNo, String strRemarks, String strInfo1, Timestamp tsDateStmt,
			String strForeignStmtTable, Long iSettleStmtId, Integer usrid) throws Exception
	{
		String strErrMsg = null;
		String strAmtInWords = CurrencyFormat.toWords(bdPaymentAmt, "", "");
		strAmtInWords = strAmtInWords + " ONLY";
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
		NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER, iDefPCCenterId,
				iCustId, "MYR");
		if (naObj == null)
		{
			naObj = new NominalAccountObject();
			// naObj.pkid = new Integer("0");
			// naObj.code = new String("not_used");
			naObj.namespace = NominalAccountBean.NS_CUSTOMER;
			naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
			naObj.foreignKey = iCustId;
			naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
			// naObj.currency = thisCurr;
			naObj.currency = "MYR";
			// naObj.amount = naObj.amount.add(lTxAmt);
			naObj.remarks = strRemarks;
			naObj.accPCCenterId = iDefPCCenterId;
			naObj.state = NominalAccountBean.STATE_CREATED;
			naObj.status = NominalAccountBean.STATUS_ACTIVE;
			naObj.lastUpdate = TimeFormat.getTimestamp();
			naObj.userIdUpdate = usrid;
			NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
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

	private void cancelInv(String docNo, Integer userId) throws Exception
	{
		String cancelSelect =
		// "select sales_txn_id from cust_invoice_index where remarks ~* '" +
		// docNo + "'";
		"select sales_txn_id from cust_invoice_index where remarks ILIKE '%" + docNo + "%'";
		Statement cancelStmt = jbossCon.createStatement();
		ResultSet rs = cancelStmt.executeQuery(cancelSelect);
		if (rs.next())
		{
			Long salesTxnId = new Long(rs.getLong("sales_txn_id"));
			//SalesTxnNut.cancelTxn(salesTxnId, userId);
		}
	}

	private void cancelGenStmt(String docNo) throws Exception
	{
		String cancelSelect =
		// "select pkid from acc_generic_stmt where remarks ~* '" + docNo + "'";
		"select pkid from acc_generic_stmt where remarks ILIKE '%" + docNo + "%'";
		Statement cancelStmt = jbossCon.createStatement();
		ResultSet rs = cancelStmt.executeQuery(cancelSelect);
		if (rs.next())
		{
			Long genStmtId = new Long(rs.getLong("pkid"));
			GenericStmtNut.cancel(genStmtId);
		}
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
 (NEW) 
 Aug 04 DB seems to have solved a few of these discrepancies in the Jul 04 DB.

 CustID solved:
 MA0014
 ME0031
 ME0033 ?? need to issue DN to reverse OR20303
 MC0103 Change CN of 765 t0 716.5
 MK0015
 SS0080
 SW0004

 (NEW) SU0048
 Action: Issue CN to reduce amount for R/OR190104

 (NEW) MS0010
 The following Invoices / DNs were invalidated by topcon, hence has to be reversed:

 docno  | txtype | ldebitamt | lcreditamt | txstatus
 ---------+--------+-----------+------------+----------
 SM_476  | I      | 4450.0000 |     0.0000 | C
 SM_745  | I      | 2300.0000 |     0.0000 | C
 VM_1731 | I      |   92.0000 |     0.0000 | C
 VM_1756 | I      |  295.0000 |     0.0000 | C
 VM_1880 | I      |  105.0000 |     0.0000 | C
 DN1880  | D      |  105.0000 |     0.0000 | C


 **/
/**
 * These are the last few fixes ...
 *  * MA0014 (OR16506 both RP and P) Action: Issue Receipt to account for
 * OR16506
 *  * MC0014 CITY OPTICAL-KL 77834904 -21370.00 (MED410 oversettled SM634 +
 * RV11782 not in payrefd) Action: Issue CN to reverse RV11782 (1130.00)
 *  * MC0103 (O/R52473 is not found in payrefd for MC0103) Action: (1) Issue
 * Receipt of 48.00 to settle VM4626 (2) Issue CN of 765.00 to make up the
 * negative balance ????? dunno why !!
 *  * ME0031 (OR14097 both RP and P) Action: Issue CN to account for OR14097
 *  * ME0033 (O/R17437 not found in cdhis) Action: Issue Receipt O/R17437* for
 * SM1117
 *  * MK0015 (OR15621 unaccounted for) Action: Issue Receipt OR15621* for SM913
 *  * MS0010 (SM476,SM745,VM1731, VM1756,VM1880 overpaid, CN1880 Credited for
 * nothing (both C and I) Action: Issue DN to reverse CM1880
 *  * SM0040 (OR8814 overpayment for nonexistant RV8814 DN) Action: Issue DN to
 * reverse OR8814
 *  * SP0046 PRAI CONSTRN.& 04399 9006 -100.00 (O/R19126 unaccounted for - not
 * found in payrefd -exception!!) Action: Issue DN to reverse O/R19126
 *  * SS0080 (O/R17435 that pays for SS10258 is DELETED!!) Action: Issue Receipt
 * for SS10258 (1250.00)
 *  * SW0004 (OR12495 paid TWICE for SS1655) Action: Issue Receipt using
 * OR12495* to close SS1655 (payrefd has dup entries)
 * 
 */
public class DoTopConFixMisc implements Action
{
	private String strClassName = "DoTopConFixMisc";
	/***************************************************************************
	 * GLOBAL VARIABLES
	 **************************************************************************/
	Integer iDefLocId = new Integer(1000);
	Integer iDefSvcCtrId = new Integer(1);
	Integer iDefPCCenterId = new Integer(1000);
	Integer iDefCondId = new Integer(StockNut.STK_COND_GOOD);
	Integer iDefCashAccId = new Integer(1000);
	Integer iDefChequeAccId = new Integer(1001);
	String strDefCurr = "MYR";
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
	private BranchObject defBranchObj = null;
	Connection con = null;
	Connection jbossCon = null;

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Get processing start time
		Timestamp tsStart = TimeFormat.getTimestamp();
		if (defBranchObj == null)
			defBranchObj = BranchNut.getObject(iDefSvcCtrId);
		/*
		 * HashMap hmCurr = new HashMap(); hmCurr.put(new Integer(1), "MYR");
		 * hmCurr.put(new Integer(2), "USD"); hmCurr.put(new Integer(3), "SGD");
		 * hmCurr.put(new Integer(4), "4"); hmCurr.put(new Integer(5), "5");
		 * hmCurr.put(new Integer(6), "6"); hmCurr.put(new Integer(7), "7");
		 */
		try
		{
			// Connection to topcon DB
			String url = "jdbc:postgresql://localhost:5432/topcon";
			con = DriverManager.getConnection(url, "jboss", "jboss");
			// Connection to Wavelet EMP DB
			String jbossURL = "jdbc:postgresql://localhost:5432/wsemp";
			jbossCon = DriverManager.getConnection(jbossURL, "jboss", "jboss");
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
			/*
			 * String findInvQ = "select pkid from cust_invoice_index where
			 * remarks ~* ? "; PreparedStatement findInvStmt =
			 * jbossCon.prepareStatement(findInvQ);
			 * 
			 * String findCustQ = "select pkid from cust_account_index where
			 * acc_code = ? "; PreparedStatement findCustStmt =
			 * jbossCon.prepareStatement(findCustQ);
			 * 
			 * String findCurrQ = "select currid from cdhis where docno = ? ";
			 * PreparedStatement findCurrStmt = con.prepareStatement(findCurrQ);
			 * 
			 * String getDocInfo = "select * from cdhis where docno = ? ";
			 * PreparedStatement getDocInfoStmt =
			 * con.prepareStatement(getDocInfo);
			 */
			/*******************************************************************
			 * CustCode: MA0014
			 * 
			 * Fix: Issue Receipt (OR16506*) for SM1042*
			 ******************************************************************/
			// issueReceipt(con, jbossCon, "OR16506", "SM1042", usrid);
			/*******************************************************************
			 * CustCode: MC0014 Fix: Issue CN to reverse RV11782 (1130.00)
			 ******************************************************************/
			issueCN(con, jbossCon, "RV11782", usrid);
			/*******************************************************************
			 * CustCode: MC0103 Fix: (1) Issue Receipt O/R52473* for Inv. VM4626
			 * (2) Issue CN to make up negative balance (-765)
			 ******************************************************************/
			issueReceipt(con, jbossCon, "O/R52473", "VM4626", usrid);
			String getDate = "select txdate from cdhis where docno = 'OR_52473'";
			Statement getDateStmt = con.createStatement();
			ResultSet rsGetDate = getDateStmt.executeQuery(getDate);
			Timestamp txDate = TimeFormat.getTimestamp();
			if (rsGetDate.next())
			{
				txDate = rsGetDate.getTimestamp("txdate");
			}
			String strRemarks = "DERIVED: Unresolved negative balance";
			// issueNewCN(con,jbossCon,"MC0103",new BigDecimal(765.00),
			issueNewCN(con, jbossCon, "MC0103", new BigDecimal(716.50), txDate, usrid, strRemarks);
			/*******************************************************************
			 * CustCode: ME0031 Fix: Issue Receipt (OR14097*) for SM1064
			 ******************************************************************/
			// issueReceipt(con, jbossCon, "OR14097", "SM1064", usrid);
			/*******************************************************************
			 * CustCode: ME0033 Fix: Issue Receipt (O/R17437*) for SM1117
			 ******************************************************************/
			// issueNewReceipt(con, jbossCon, "ME0033", "O/R17437", "SM1117",
			// usrid);
			issueDN(con, jbossCon, "OR20303", usrid);
			/*******************************************************************
			 * CustCode: MK0015 Fix: Issue Receipt (OR15621*) for SM913
			 ******************************************************************/
			// issueReceipt(con, jbossCon, "OR15621", "SM913", usrid);
			/*******************************************************************
			 * CustCode: MS0010 Fix: Issue DN to reverse CM1880
			 ******************************************************************/
			issueDN(con, jbossCon, "CM1880", usrid);
			cancelInv("SM_476", usrid);
			cancelInv("SM_745", usrid);
			cancelInv("VM_1731", usrid);
			cancelInv("VM_1756", usrid);
			cancelInv("VM_1880", usrid);
			cancelGenStmt("DN1880");
			/*******************************************************************
			 * CustCode: SM0040 Fix: Issue DN/ReversePayment to reverse OR8814
			 * for non-existant DN RV8814
			 ******************************************************************/
			issueDN(con, jbossCon, "OR8814", usrid);
			/*******************************************************************
			 * CustCode: SP0046 Fix: Issue DN/ReversePayment to reverse O/R19126
			 ******************************************************************/
			issueDN(con, jbossCon, "O/R19126", usrid);
			/*******************************************************************
			 * CustCode: SS0080 Fix: Issue Receipt O/R17435 for Inv. SS10258
			 ******************************************************************/
			// issueReceipt(con, jbossCon, "O/R17435", "SS10258", usrid);
			/*******************************************************************
			 * CustCode: SU0048 Fix: Issue Receipt OR12495 for Inv. SS1655
			 ******************************************************************/
			getDate = "select txdate from cdhis where docno = 'R/OR190104'";
			rsGetDate = getDateStmt.executeQuery(getDate);
			txDate = TimeFormat.getTimestamp();
			if (rsGetDate.next())
			{
				txDate = rsGetDate.getTimestamp("txdate");
			}
			strRemarks = "DERIVED: Correct Reverse Payment R/OR190104";
			// issueNewCN(con,jbossCon,"MC0103",new BigDecimal(765.00),
			issueNewCN(con, jbossCon, "SU0048", new BigDecimal(500), txDate, usrid, strRemarks);
			/*******************************************************************
			 * CustCode: SW0004 Fix: Issue Receipt OR12495 for Inv. SS1655
			 ******************************************************************/
			// issueReceipt(con, jbossCon, "OR12495", "SS1655", usrid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while fixing NAT: " + ex.getMessage());
		}
		Log.printVerbose("***** END: FIX MISCELLANEOUS *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "FIX MISCELLANEOUS");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		return new ActionRouter("test-migrate-topcondb-page");
	}

	private void issueReceipt(Connection topconCon, Connection jbossCon, String payNo, String docRefNo, Integer usrid)
			throws Exception
	{
		String findInvQ =
		// "select pkid from cust_invoice_index where remarks ~* ? ";
		"select pkid from cust_invoice_index where remarks ILIKE ? ";
		PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		String getDocInfo = "select * from cdhis where docno = ? ";
		PreparedStatement getDocInfoStmt = topconCon.prepareStatement(getDocInfo);
		String strCustId = null;
		Integer iCustId = null;
		String strDocNo = null;
		String strCurr = null;
		String strRemarks = "DERIVED PAYMENT (Old DocRef = " + payNo + "**)";
		BigDecimal bdAmount = null;
		Timestamp tsTxDate = null;
		// Get the doc info
		getDocInfoStmt.setString(1, payNo);
		ResultSet rsGetDocInfo = getDocInfoStmt.executeQuery();
		if (rsGetDocInfo.next())
		{
			strCustId = rsGetDocInfo.getString("custid");
			strCurr = rsGetDocInfo.getString("currid");
			strCurr = (String) hmCurr.get(new Integer(strCurr));
			strDocNo = rsGetDocInfo.getString("docno");
			tsTxDate = rsGetDocInfo.getTimestamp("txdate");
			bdAmount = rsGetDocInfo.getBigDecimal("lcreditamt");
			// strRemarks = rsGetDocInfo.getString("comment");
		}
		getDocInfoStmt.close();
		Log.printDebug("##### Processing Cust " + strCustId + ": " + payNo + " #####");
		// Get the Customer Id
		findCustStmt.setString(1, strCustId);
		ResultSet rsFindCust = findCustStmt.executeQuery();
		if (rsFindCust.next())
		{
			iCustId = new Integer(rsFindCust.getInt("pkid"));
		}
		findCustStmt.close();
		// Query for Details from CDHisD
		String txnDQuery = "select desc1 from cdhisd where docref = '" + payNo + "'";
		Statement txnDStmt = topconCon.createStatement();
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
			strRemarks = strRemarks + " (DESC=" + desc1 + ")";
		}
		txnDStmt.close();
		Long newRcptId = createCustReceipt(iCustId, iCashAccId, tsTxDate, tsTxDate, tsTxDate, strCurr, bdAmount,
				strPayMethod, strRemarks, strChequeNo, // "" for Cash
				usrid, TimeFormat.getTimestamp());
		Log.printVerbose("Successfully created Receipt " + payNo);
		// Get the Invoice Id
		Long refDocId = new Long(0);
		findInvStmt.setString(1, "% = " + docRefNo + ")%");
		ResultSet rsFindInv = findInvStmt.executeQuery();
		if (rsFindInv.next())
		{
			refDocId = new Long(rsFindInv.getLong("pkid"));
		}
		// Create the doclink
		DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_ADJ,
		// ReceiptBean.TABLENAME, newRcptId,
				OfficialReceiptBean.TABLENAME, newRcptId, InvoiceBean.TABLENAME, refDocId, strCurr, bdAmount.negate(),
				"", bdAmount.negate(), "", TimeFormat.getTimestamp(), usrid);
	}

	private void issueNewReceipt(Connection topconCon, Connection jbossCon, String strCustId, String payNo,
			String docRefNo, Integer usrid) throws Exception
	{
		String findInvQ =
		// "select pkid from cust_invoice_index where remarks ~* ? ";
		"select pkid from cust_invoice_index where remarks ILIKE ? ";
		PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		Integer iCustId = null;
		Log.printDebug("##### Processing Cust " + strCustId + ": " + payNo + " #####");
		// Get the Customer Id
		findCustStmt.setString(1, strCustId);
		ResultSet rsFindCust = findCustStmt.executeQuery();
		if (rsFindCust.next())
		{
			iCustId = new Integer(rsFindCust.getInt("pkid"));
		}
		findCustStmt.close();
		/*
		 * // Query for Details from CDHisD String txnDQuery = "select desc1
		 * from cdhisd where docref = '" + payNo + "'"; Statement txnDStmt =
		 * con.createStatement(); ResultSet rsTxnD =
		 * txnDStmt.executeQuery(txnDQuery);
		 * 
		 * String strPayMethod = "cash"; // default to cash String strChequeNo =
		 * ""; Integer iCashAccId = iDefCashAccId; //while(rsTxnD.next())
		 * if(rsTxnD.next()) { //String glId = rsTxnD.getString("glid"); String
		 * desc1 = rsTxnD.getString("desc1").trim();
		 * 
		 * if (!desc1.equals("") && !desc1.startsWith("0/")) { strPayMethod =
		 * "cheque"; strChequeNo = desc1; iCashAccId = iDefChequeAccId; }
		 * //remarks = remarks + ", (GLID=" + glId strRemarks = strRemarks + "
		 * (DESC=" + desc1 + ")"; } txnDStmt.close();
		 */
		String getPaymentInfo = "select paydate,lpayamt from payrefd where payno = '" + payNo + "'";
		Statement getPaymentInfoStmt = topconCon.createStatement();
		ResultSet rsPymtInfo = getPaymentInfoStmt.executeQuery(getPaymentInfo);
		Timestamp tsTxDate = null;
		BigDecimal bdAmount = null;
		if (rsPymtInfo.next())
		{
			tsTxDate = TimeFormat.createTimeStamp(rsPymtInfo.getString("paydate"), "MM/dd/yy HH:mm:ss");
			bdAmount = rsPymtInfo.getBigDecimal("lpayamt");
		}
		String strRemarks = "DERIVED PAYMENT (Old DocRef = " + payNo + "**)";
		Long newRcptId = createCustReceipt(iCustId, iDefCashAccId, tsTxDate, tsTxDate, tsTxDate, "MYR", bdAmount,
				"cash", strRemarks, "", // "" for Cash
				usrid, TimeFormat.getTimestamp());
		Log.printVerbose("Successfully created Receipt " + payNo);
		// Get the Invoice Id
		Long refDocId = new Long(0);
		findInvStmt.setString(1, "% = " + docRefNo + ")%");
		ResultSet rsFindInv = findInvStmt.executeQuery();
		if (rsFindInv.next())
		{
			refDocId = new Long(rsFindInv.getLong("pkid"));
		}
		// Create the doclink
		DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_ADJ,
		// ReceiptBean.TABLENAME, newRcptId,
				OfficialReceiptBean.TABLENAME, newRcptId, InvoiceBean.TABLENAME, refDocId, "MYR", bdAmount.negate(),
				"", bdAmount.negate(), "", TimeFormat.getTimestamp(), usrid);
	}

	private void issueCN(Connection topconCon, Connection jbossCon, String docRefNo, Integer usrid) throws Exception
	{
		String findInvQ =
		// "select pkid from cust_invoice_index where remarks ~* ? ";
		"select pkid from cust_invoice_index where remarks ILIKE ? ";
		PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
		String findGenQ =
		// "select pkid from acc_generic_stmt where remarks ~* ? ";
		"select pkid from acc_generic_stmt where remarks ILIKE ? ";
		PreparedStatement findGenStmt = jbossCon.prepareStatement(findGenQ);
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		String getDocInfo = "select * from cdhis where docno = ? ";
		PreparedStatement getDocInfoStmt = topconCon.prepareStatement(getDocInfo);
		String strCustId = null;
		Integer iCustId = null;
		String strDocNo = null;
		String strCurr = null;
		String strTxType = null;
		String strRemarks = "DERIVED: To Reverse " + docRefNo;
		BigDecimal bdAmount = null;
		Timestamp tsTxDate = null;
		// Get the doc info
		getDocInfoStmt.setString(1, docRefNo);
		ResultSet rsGetDocInfo = getDocInfoStmt.executeQuery();
		if (rsGetDocInfo.next())
		{
			strCustId = rsGetDocInfo.getString("custid");
			strCurr = rsGetDocInfo.getString("currid");
			strCurr = (String) hmCurr.get(new Integer(strCurr));
			strDocNo = rsGetDocInfo.getString("docno");
			strTxType = rsGetDocInfo.getString("txtype");
			tsTxDate = rsGetDocInfo.getTimestamp("txdate");
			// bdAmount = rsGetDocInfo.getBigDecimal("lcreditamt");
			bdAmount = rsGetDocInfo.getBigDecimal("ldebitamt");
			// strRemarks = rsGetDocInfo.getString("comment");
		}
		getDocInfoStmt.close();
		Log.printDebug("##### Processing Cust " + strCustId + ": DocRef = " + docRefNo + " #####");
		// Get the Customer Id
		findCustStmt.setString(1, strCustId);
		ResultSet rsFindCust = findCustStmt.executeQuery();
		if (rsFindCust.next())
		{
			iCustId = new Integer(rsFindCust.getInt("pkid"));
		}
		findCustStmt.close();
		Long refDocId = new Long(0);
		String refDocTable = null;
		// Depends on what we are reversing, could be I, DN, or RP
		if (strTxType.equals("I"))
		{
			// Get the Invoice Id
			findInvStmt.setString(1, "% = " + docRefNo + ")%");
			ResultSet rsFindInv = findInvStmt.executeQuery();
			if (rsFindInv.next())
			{
				refDocId = new Long(rsFindInv.getLong("pkid"));
				refDocTable = InvoiceBean.TABLENAME;
			}
		} else
		{
			// Get the GenericStmt Id
			findGenStmt.setString(1, "% = " + docRefNo + ")%");
			ResultSet rsFindGenStmt = findGenStmt.executeQuery();
			if (rsFindGenStmt.next())
			{
				refDocId = new Long(rsFindGenStmt.getLong("pkid"));
				refDocTable = GenericStmtBean.TABLENAME;
			}
		}
		Long newCNId = createCreditNote(iCustId, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE, GLCodeBean.CASH_DISCOUNT,
				tsTxDate, refDocTable, refDocId, docRefNo, strRemarks, TimeFormat.getTimestamp(),
				// usrid, strCurr, bdAmount);
				usrid, strDefCurr, bdAmount);
		// Create the doclink
		DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_ADJ,
				GenericStmtBean.TABLENAME, newCNId, refDocTable, refDocId, strCurr, bdAmount.negate(), "",
				bdAmount.negate(), "", TimeFormat.getTimestamp(), usrid);
	}

	private void issueNewCN(Connection topconCon, Connection jbossCon, String strCustId, BigDecimal bdAmount,
			Timestamp txDate, Integer usrid, String remarks) throws Exception
	{
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		String strRemarks = "DERIVED: Unresolved negative balance";
		Log.printDebug("##### Processing Cust " + strCustId + ": NEW CREDIT NOTE");
		// Get the Customer Id
		Integer iCustId = null;
		findCustStmt.setString(1, strCustId);
		ResultSet rsFindCust = findCustStmt.executeQuery();
		if (rsFindCust.next())
		{
			iCustId = new Integer(rsFindCust.getInt("pkid"));
		}
		findCustStmt.close();
		Long newCNId = createCreditNote(iCustId, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE, GLCodeBean.CASH_DISCOUNT,
				txDate, "", new Long(0), "", strRemarks, TimeFormat.getTimestamp(), usrid, strDefCurr, bdAmount);
	}

	private void issueDN(Connection topconCon, Connection jbossCon, String docRefNo, Integer usrid) throws Exception
	{
		String findInvQ =
		// "select pkid from cust_receipt_index where payment_remarks ~* ? ";
		"select pkid from cust_receipt_index where payment_remarks ILIKE ? ";
		PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
		String findGenQ = "select pkid from acc_generic_stmt where remarks ILIKE ? ";
		PreparedStatement findGenStmt = jbossCon.prepareStatement(findGenQ);
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		String getDocInfo = "select * from cdhis where docno = ? ";
		PreparedStatement getDocInfoStmt = topconCon.prepareStatement(getDocInfo);
		String strCustId = null;
		Integer iCustId = null;
		String strDocNo = null;
		String strCurr = null;
		String strTxType = null;
		String strRemarks = "DERIVED: To Reverse " + docRefNo;
		BigDecimal bdAmount = null;
		Timestamp tsTxDate = null;
		// Get the doc info
		getDocInfoStmt.setString(1, docRefNo);
		ResultSet rsGetDocInfo = getDocInfoStmt.executeQuery();
		if (rsGetDocInfo.next())
		{
			strCustId = rsGetDocInfo.getString("custid");
			strCurr = rsGetDocInfo.getString("currid");
			strCurr = (String) hmCurr.get(new Integer(strCurr));
			strDocNo = rsGetDocInfo.getString("docno");
			strTxType = rsGetDocInfo.getString("txtype");
			tsTxDate = rsGetDocInfo.getTimestamp("txdate");
			bdAmount = rsGetDocInfo.getBigDecimal("lcreditamt");
			// strRemarks = rsGetDocInfo.getString("comment");
		}
		if (!strCurr.equals(strDefCurr))
		{
			strRemarks += ", (CURR=" + strCurr + ")";
		}
		getDocInfoStmt.close();
		Log.printDebug("##### Processing Cust " + strCustId + ": DocRef = " + docRefNo + " #####");
		// Get the Customer Id
		findCustStmt.setString(1, strCustId);
		ResultSet rsFindCust = findCustStmt.executeQuery();
		if (rsFindCust.next())
		{
			iCustId = new Integer(rsFindCust.getInt("pkid"));
		}
		findCustStmt.close();
		Long refDocId = new Long(0);
		String refDocTable = null;
		// Depends on what we are reversing, could be C or P
		if (strTxType.equals("P"))
		{
			// Get the Receipt Id
			findInvStmt.setString(1, "% = " + docRefNo + ")%");
			ResultSet rsFindInv = findInvStmt.executeQuery();
			if (rsFindInv.next())
			{
				refDocId = new Long(rsFindInv.getLong("pkid"));
				// refDocTable = ReceiptBean.TABLENAME;
				refDocTable = OfficialReceiptBean.TABLENAME;
			}
		} else
		{
			// Get the GenericStmt Id
			findGenStmt.setString(1, "% = " + docRefNo + ")%");
			ResultSet rsFindGenStmt = findGenStmt.executeQuery();
			if (rsFindGenStmt.next())
			{
				refDocId = new Long(rsFindGenStmt.getLong("pkid"));
				refDocTable = GenericStmtBean.TABLENAME;
			}
		}
		Long newDNId = createDebitNote(iCustId, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE, GLCodeBean.INTEREST_REVENUE,
				tsTxDate, refDocTable, refDocId, docRefNo, strRemarks, TimeFormat.getTimestamp(),
				// usrid, strCurr, bdAmount);
				usrid, strDefCurr, bdAmount);
		// Create the doclink
		DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_ADJ,
				GenericStmtBean.TABLENAME, newDNId, refDocTable, refDocId, strCurr, bdAmount, "", bdAmount, "",
				TimeFormat.getTimestamp(), usrid);
	}

	private Long createCustReceipt(Integer custId, // naPkid,
			Integer caPkid, Timestamp tsReceiptDate, Timestamp tsEffFromDate, Timestamp tsEffToDate, String currency,
			BigDecimal bdPaymentAmt, String strPaymentMethod, String strPaymentRmks,
			// String strGLCodeCredit,
			// String strGLCodeDebit,
			String strChequeNumber,
			// String strCashAccount,
			Integer iUsrId, Timestamp tsCreate) throws Exception
	{
		Long salesTxnId = new Long("0");
		String strAmountStr = CurrencyFormat.toWords(bdPaymentAmt, "", "");
		strAmountStr = strAmountStr + " ONLY";
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
			naObj.remarks = strPaymentRmks;
			naObj.accPCCenterId = iDefPCCenterId;
			naObj.state = NominalAccountBean.STATE_CREATED;
			naObj.status = NominalAccountBean.STATUS_ACTIVE;
			naObj.lastUpdate = TimeFormat.getTimestamp();
			naObj.userIdUpdate = iUsrId;
			NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
		}
		// / Get objects based on parameters above
		// NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
		CashAccountObject caObj = CashAccountNut.getObject(caPkid);
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
		/*
		 * NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		 * natObj.nominalAccount = naObj.pkid; natObj.foreignTable =
		 * NominalAccountTxnBean.FT_CUST_RECEIPT; natObj.foreignKey =
		 * cRcptObj.pkid; natObj.code = "not_used"; natObj.info1 = "";
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
		 * if(natEJB == null) { try { sPayEJB.remove();} catch(Exception ex) {
		 * ex.printStackTrace();} throw new Exception("Failed to create NAT"); }
		 * /// 4) update cash account transactions CashAccTxnObject catObj = new
		 * CashAccTxnObject(); catObj.txnCode = CashAccTxnBean.TXN_CODE_DEFAULT;
		 * catObj.glCodeCredit = GLCodeBean.ACC_RECEIVABLE; catObj.glCodeDebit =
		 * caObj.accountType; catObj.personInCharge = iUsrId; catObj.accFrom =
		 * new Integer("0");// another known bank account catObj.accTo =
		 * caObj.pkId; catObj.foreignTable = CashAccTxnBean.FT_CUST_RECEIPT;
		 * catObj.foreignKey = cRcptObj.pkid; catObj.currency = naObj.currency;
		 * catObj.amount = bdPaymentAmt; catObj.txnTime = tsReceiptDate;
		 * catObj.remarks = strPaymentRmks; catObj.info1 = ""; catObj.info2 =
		 * ""; catObj.state = CashAccTxnBean.ST_CREATED; catObj.status =
		 * CashAccTxnBean.STATUS_ACTIVE; catObj.lastUpdate = tsCreate;
		 * catObj.userIdUpdate = iUsrId; catObj.pcCenter = caObj.pcCenter;
		 * CashAccTxn catEJB = CashAccTxnNut.fnCreate(catObj); /// 5) update
		 * cash account balance // no need to set the balance !!! hurray !!!!
		 * 
		 * /// 3) update the nominal account NominalAccount naEJB =
		 * NominalAccountNut.getHandle(naObj.pkid); BigDecimal bdLatestBal =
		 * naEJB.getAmount(); bdLatestBal = bdLatestBal.add(natObj.amount);
		 * naObj.amount = bdLatestBal;
		 */
		// return sPayEJB.getPkid();
		return receiptObj.pkid;
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

	private Long createReversePayment(String strStmtType,
	// Integer iNominalAccId,
			Integer iCustId, Integer iCashAccountId, String currency, BigDecimal bdPaymentAmt,
			String strChequeCreditCardNo, String strRemarks, String strInfo1, Timestamp tsDateStmt,
			String strForeignStmtTable, Long iSettleStmtId, Integer usrid) throws Exception
	{
		String strErrMsg = null;
		String strAmtInWords = CurrencyFormat.toWords(bdPaymentAmt, "", "");
		strAmtInWords = strAmtInWords + " ONLY";
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
		NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER, iDefPCCenterId,
				iCustId, "MYR");
		if (naObj == null)
		{
			naObj = new NominalAccountObject();
			// naObj.pkid = new Integer("0");
			// naObj.code = new String("not_used");
			naObj.namespace = NominalAccountBean.NS_CUSTOMER;
			naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
			naObj.foreignKey = iCustId;
			naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
			// naObj.currency = thisCurr;
			naObj.currency = "MYR";
			// naObj.amount = naObj.amount.add(lTxAmt);
			naObj.remarks = strRemarks;
			naObj.accPCCenterId = iDefPCCenterId;
			naObj.state = NominalAccountBean.STATE_CREATED;
			naObj.status = NominalAccountBean.STATUS_ACTIVE;
			naObj.lastUpdate = TimeFormat.getTimestamp();
			naObj.userIdUpdate = usrid;
			NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
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

	private void cancelInv(String docNo, Integer userId) throws Exception
	{
		String cancelSelect =
		// "select sales_txn_id from cust_invoice_index where remarks ~* '" +
		// docNo + "'";
		"select sales_txn_id from cust_invoice_index where remarks ILIKE '%" + docNo + "%'";
		Statement cancelStmt = jbossCon.createStatement();
		ResultSet rs = cancelStmt.executeQuery(cancelSelect);
		if (rs.next())
		{
			Long salesTxnId = new Long(rs.getLong("sales_txn_id"));
			//SalesTxnNut.cancelTxn(salesTxnId, userId);
		}
	}

	private void cancelGenStmt(String docNo) throws Exception
	{
		String cancelSelect =
		// "select pkid from acc_generic_stmt where remarks ~* '" + docNo + "'";
		"select pkid from acc_generic_stmt where remarks ILIKE '%" + docNo + "%'";
		Statement cancelStmt = jbossCon.createStatement();
		ResultSet rs = cancelStmt.executeQuery(cancelSelect);
		if (rs.next())
		{
			Long genStmtId = new Long(rs.getLong("pkid"));
			GenericStmtNut.cancel(genStmtId);
		}
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
 (NEW) 
 Aug 04 DB seems to have solved a few of these discrepancies in the Jul 04 DB.

 CustID solved:
 MA0014
 ME0031
 ME0033 ?? need to issue DN to reverse OR20303
 MC0103 Change CN of 765 t0 716.5
 MK0015
 SS0080
 SW0004

 (NEW) SU0048
 Action: Issue CN to reduce amount for R/OR190104

 (NEW) MS0010
 The following Invoices / DNs were invalidated by topcon, hence has to be reversed:

 docno  | txtype | ldebitamt | lcreditamt | txstatus
 ---------+--------+-----------+------------+----------
 SM_476  | I      | 4450.0000 |     0.0000 | C
 SM_745  | I      | 2300.0000 |     0.0000 | C
 VM_1731 | I      |   92.0000 |     0.0000 | C
 VM_1756 | I      |  295.0000 |     0.0000 | C
 VM_1880 | I      |  105.0000 |     0.0000 | C
 DN1880  | D      |  105.0000 |     0.0000 | C


 **/
/**
 * These are the last few fixes ...
 *  * MA0014 (OR16506 both RP and P) Action: Issue Receipt to account for
 * OR16506
 *  * MC0014 CITY OPTICAL-KL 77834904 -21370.00 (MED410 oversettled SM634 +
 * RV11782 not in payrefd) Action: Issue CN to reverse RV11782 (1130.00)
 *  * MC0103 (O/R52473 is not found in payrefd for MC0103) Action: (1) Issue
 * Receipt of 48.00 to settle VM4626 (2) Issue CN of 765.00 to make up the
 * negative balance ????? dunno why !!
 *  * ME0031 (OR14097 both RP and P) Action: Issue CN to account for OR14097
 *  * ME0033 (O/R17437 not found in cdhis) Action: Issue Receipt O/R17437* for
 * SM1117
 *  * MK0015 (OR15621 unaccounted for) Action: Issue Receipt OR15621* for SM913
 *  * MS0010 (SM476,SM745,VM1731, VM1756,VM1880 overpaid, CN1880 Credited for
 * nothing (both C and I) Action: Issue DN to reverse CM1880
 *  * SM0040 (OR8814 overpayment for nonexistant RV8814 DN) Action: Issue DN to
 * reverse OR8814
 *  * SP0046 PRAI CONSTRN.& 04399 9006 -100.00 (O/R19126 unaccounted for - not
 * found in payrefd -exception!!) Action: Issue DN to reverse O/R19126
 *  * SS0080 (O/R17435 that pays for SS10258 is DELETED!!) Action: Issue Receipt
 * for SS10258 (1250.00)
 *  * SW0004 (OR12495 paid TWICE for SS1655) Action: Issue Receipt using
 * OR12495* to close SS1655 (payrefd has dup entries)
 * 
 */
public class DoTopConFixMisc implements Action
{
	private String strClassName = "DoTopConFixMisc";
	/***************************************************************************
	 * GLOBAL VARIABLES
	 **************************************************************************/
	Integer iDefLocId = new Integer(1000);
	Integer iDefSvcCtrId = new Integer(1);
	Integer iDefPCCenterId = new Integer(1000);
	Integer iDefCondId = new Integer(StockNut.STK_COND_GOOD);
	Integer iDefCashAccId = new Integer(1000);
	Integer iDefChequeAccId = new Integer(1001);
	String strDefCurr = "MYR";
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
	private BranchObject defBranchObj = null;
	Connection con = null;
	Connection jbossCon = null;

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Get processing start time
		Timestamp tsStart = TimeFormat.getTimestamp();
		if (defBranchObj == null)
			defBranchObj = BranchNut.getObject(iDefSvcCtrId);
		/*
		 * HashMap hmCurr = new HashMap(); hmCurr.put(new Integer(1), "MYR");
		 * hmCurr.put(new Integer(2), "USD"); hmCurr.put(new Integer(3), "SGD");
		 * hmCurr.put(new Integer(4), "4"); hmCurr.put(new Integer(5), "5");
		 * hmCurr.put(new Integer(6), "6"); hmCurr.put(new Integer(7), "7");
		 */
		try
		{
			// Connection to topcon DB
			String url = "jdbc:postgresql://localhost:5432/topcon";
			con = DriverManager.getConnection(url, "jboss", "jboss");
			// Connection to Wavelet EMP DB
			String jbossURL = "jdbc:postgresql://localhost:5432/wsemp";
			jbossCon = DriverManager.getConnection(jbossURL, "jboss", "jboss");
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
			/*
			 * String findInvQ = "select pkid from cust_invoice_index where
			 * remarks ~* ? "; PreparedStatement findInvStmt =
			 * jbossCon.prepareStatement(findInvQ);
			 * 
			 * String findCustQ = "select pkid from cust_account_index where
			 * acc_code = ? "; PreparedStatement findCustStmt =
			 * jbossCon.prepareStatement(findCustQ);
			 * 
			 * String findCurrQ = "select currid from cdhis where docno = ? ";
			 * PreparedStatement findCurrStmt = con.prepareStatement(findCurrQ);
			 * 
			 * String getDocInfo = "select * from cdhis where docno = ? ";
			 * PreparedStatement getDocInfoStmt =
			 * con.prepareStatement(getDocInfo);
			 */
			/*******************************************************************
			 * CustCode: MA0014
			 * 
			 * Fix: Issue Receipt (OR16506*) for SM1042*
			 ******************************************************************/
			// issueReceipt(con, jbossCon, "OR16506", "SM1042", usrid);
			/*******************************************************************
			 * CustCode: MC0014 Fix: Issue CN to reverse RV11782 (1130.00)
			 ******************************************************************/
			issueCN(con, jbossCon, "RV11782", usrid);
			/*******************************************************************
			 * CustCode: MC0103 Fix: (1) Issue Receipt O/R52473* for Inv. VM4626
			 * (2) Issue CN to make up negative balance (-765)
			 ******************************************************************/
			issueReceipt(con, jbossCon, "O/R52473", "VM4626", usrid);
			String getDate = "select txdate from cdhis where docno = 'OR_52473'";
			Statement getDateStmt = con.createStatement();
			ResultSet rsGetDate = getDateStmt.executeQuery(getDate);
			Timestamp txDate = TimeFormat.getTimestamp();
			if (rsGetDate.next())
			{
				txDate = rsGetDate.getTimestamp("txdate");
			}
			String strRemarks = "DERIVED: Unresolved negative balance";
			// issueNewCN(con,jbossCon,"MC0103",new BigDecimal(765.00),
			issueNewCN(con, jbossCon, "MC0103", new BigDecimal(716.50), txDate, usrid, strRemarks);
			/*******************************************************************
			 * CustCode: ME0031 Fix: Issue Receipt (OR14097*) for SM1064
			 ******************************************************************/
			// issueReceipt(con, jbossCon, "OR14097", "SM1064", usrid);
			/*******************************************************************
			 * CustCode: ME0033 Fix: Issue Receipt (O/R17437*) for SM1117
			 ******************************************************************/
			// issueNewReceipt(con, jbossCon, "ME0033", "O/R17437", "SM1117",
			// usrid);
			issueDN(con, jbossCon, "OR20303", usrid);
			/*******************************************************************
			 * CustCode: MK0015 Fix: Issue Receipt (OR15621*) for SM913
			 ******************************************************************/
			// issueReceipt(con, jbossCon, "OR15621", "SM913", usrid);
			/*******************************************************************
			 * CustCode: MS0010 Fix: Issue DN to reverse CM1880
			 ******************************************************************/
			issueDN(con, jbossCon, "CM1880", usrid);
			cancelInv("SM_476", usrid);
			cancelInv("SM_745", usrid);
			cancelInv("VM_1731", usrid);
			cancelInv("VM_1756", usrid);
			cancelInv("VM_1880", usrid);
			cancelGenStmt("DN1880");
			/*******************************************************************
			 * CustCode: SM0040 Fix: Issue DN/ReversePayment to reverse OR8814
			 * for non-existant DN RV8814
			 ******************************************************************/
			issueDN(con, jbossCon, "OR8814", usrid);
			/*******************************************************************
			 * CustCode: SP0046 Fix: Issue DN/ReversePayment to reverse O/R19126
			 ******************************************************************/
			issueDN(con, jbossCon, "O/R19126", usrid);
			/*******************************************************************
			 * CustCode: SS0080 Fix: Issue Receipt O/R17435 for Inv. SS10258
			 ******************************************************************/
			// issueReceipt(con, jbossCon, "O/R17435", "SS10258", usrid);
			/*******************************************************************
			 * CustCode: SU0048 Fix: Issue Receipt OR12495 for Inv. SS1655
			 ******************************************************************/
			getDate = "select txdate from cdhis where docno = 'R/OR190104'";
			rsGetDate = getDateStmt.executeQuery(getDate);
			txDate = TimeFormat.getTimestamp();
			if (rsGetDate.next())
			{
				txDate = rsGetDate.getTimestamp("txdate");
			}
			strRemarks = "DERIVED: Correct Reverse Payment R/OR190104";
			// issueNewCN(con,jbossCon,"MC0103",new BigDecimal(765.00),
			issueNewCN(con, jbossCon, "SU0048", new BigDecimal(500), txDate, usrid, strRemarks);
			/*******************************************************************
			 * CustCode: SW0004 Fix: Issue Receipt OR12495 for Inv. SS1655
			 ******************************************************************/
			// issueReceipt(con, jbossCon, "OR12495", "SS1655", usrid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while fixing NAT: " + ex.getMessage());
		}
		Log.printVerbose("***** END: FIX MISCELLANEOUS *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "FIX MISCELLANEOUS");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		return new ActionRouter("test-migrate-topcondb-page");
	}

	private void issueReceipt(Connection topconCon, Connection jbossCon, String payNo, String docRefNo, Integer usrid)
			throws Exception
	{
		String findInvQ =
		// "select pkid from cust_invoice_index where remarks ~* ? ";
		"select pkid from cust_invoice_index where remarks ILIKE ? ";
		PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		String getDocInfo = "select * from cdhis where docno = ? ";
		PreparedStatement getDocInfoStmt = topconCon.prepareStatement(getDocInfo);
		String strCustId = null;
		Integer iCustId = null;
		String strDocNo = null;
		String strCurr = null;
		String strRemarks = "DERIVED PAYMENT (Old DocRef = " + payNo + "**)";
		BigDecimal bdAmount = null;
		Timestamp tsTxDate = null;
		// Get the doc info
		getDocInfoStmt.setString(1, payNo);
		ResultSet rsGetDocInfo = getDocInfoStmt.executeQuery();
		if (rsGetDocInfo.next())
		{
			strCustId = rsGetDocInfo.getString("custid");
			strCurr = rsGetDocInfo.getString("currid");
			strCurr = (String) hmCurr.get(new Integer(strCurr));
			strDocNo = rsGetDocInfo.getString("docno");
			tsTxDate = rsGetDocInfo.getTimestamp("txdate");
			bdAmount = rsGetDocInfo.getBigDecimal("lcreditamt");
			// strRemarks = rsGetDocInfo.getString("comment");
		}
		getDocInfoStmt.close();
		Log.printDebug("##### Processing Cust " + strCustId + ": " + payNo + " #####");
		// Get the Customer Id
		findCustStmt.setString(1, strCustId);
		ResultSet rsFindCust = findCustStmt.executeQuery();
		if (rsFindCust.next())
		{
			iCustId = new Integer(rsFindCust.getInt("pkid"));
		}
		findCustStmt.close();
		// Query for Details from CDHisD
		String txnDQuery = "select desc1 from cdhisd where docref = '" + payNo + "'";
		Statement txnDStmt = topconCon.createStatement();
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
			strRemarks = strRemarks + " (DESC=" + desc1 + ")";
		}
		txnDStmt.close();
		Long newRcptId = createCustReceipt(iCustId, iCashAccId, tsTxDate, tsTxDate, tsTxDate, strCurr, bdAmount,
				strPayMethod, strRemarks, strChequeNo, // "" for Cash
				usrid, TimeFormat.getTimestamp());
		Log.printVerbose("Successfully created Receipt " + payNo);
		// Get the Invoice Id
		Long refDocId = new Long(0);
		findInvStmt.setString(1, "% = " + docRefNo + ")%");
		ResultSet rsFindInv = findInvStmt.executeQuery();
		if (rsFindInv.next())
		{
			refDocId = new Long(rsFindInv.getLong("pkid"));
		}
		// Create the doclink
		DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_ADJ,
		// ReceiptBean.TABLENAME, newRcptId,
				OfficialReceiptBean.TABLENAME, newRcptId, InvoiceBean.TABLENAME, refDocId, strCurr, bdAmount.negate(),
				"", bdAmount.negate(), "", TimeFormat.getTimestamp(), usrid);
	}

	private void issueNewReceipt(Connection topconCon, Connection jbossCon, String strCustId, String payNo,
			String docRefNo, Integer usrid) throws Exception
	{
		String findInvQ =
		// "select pkid from cust_invoice_index where remarks ~* ? ";
		"select pkid from cust_invoice_index where remarks ILIKE ? ";
		PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		Integer iCustId = null;
		Log.printDebug("##### Processing Cust " + strCustId + ": " + payNo + " #####");
		// Get the Customer Id
		findCustStmt.setString(1, strCustId);
		ResultSet rsFindCust = findCustStmt.executeQuery();
		if (rsFindCust.next())
		{
			iCustId = new Integer(rsFindCust.getInt("pkid"));
		}
		findCustStmt.close();
		/*
		 * // Query for Details from CDHisD String txnDQuery = "select desc1
		 * from cdhisd where docref = '" + payNo + "'"; Statement txnDStmt =
		 * con.createStatement(); ResultSet rsTxnD =
		 * txnDStmt.executeQuery(txnDQuery);
		 * 
		 * String strPayMethod = "cash"; // default to cash String strChequeNo =
		 * ""; Integer iCashAccId = iDefCashAccId; //while(rsTxnD.next())
		 * if(rsTxnD.next()) { //String glId = rsTxnD.getString("glid"); String
		 * desc1 = rsTxnD.getString("desc1").trim();
		 * 
		 * if (!desc1.equals("") && !desc1.startsWith("0/")) { strPayMethod =
		 * "cheque"; strChequeNo = desc1; iCashAccId = iDefChequeAccId; }
		 * //remarks = remarks + ", (GLID=" + glId strRemarks = strRemarks + "
		 * (DESC=" + desc1 + ")"; } txnDStmt.close();
		 */
		String getPaymentInfo = "select paydate,lpayamt from payrefd where payno = '" + payNo + "'";
		Statement getPaymentInfoStmt = topconCon.createStatement();
		ResultSet rsPymtInfo = getPaymentInfoStmt.executeQuery(getPaymentInfo);
		Timestamp tsTxDate = null;
		BigDecimal bdAmount = null;
		if (rsPymtInfo.next())
		{
			tsTxDate = TimeFormat.createTimeStamp(rsPymtInfo.getString("paydate"), "MM/dd/yy HH:mm:ss");
			bdAmount = rsPymtInfo.getBigDecimal("lpayamt");
		}
		String strRemarks = "DERIVED PAYMENT (Old DocRef = " + payNo + "**)";
		Long newRcptId = createCustReceipt(iCustId, iDefCashAccId, tsTxDate, tsTxDate, tsTxDate, "MYR", bdAmount,
				"cash", strRemarks, "", // "" for Cash
				usrid, TimeFormat.getTimestamp());
		Log.printVerbose("Successfully created Receipt " + payNo);
		// Get the Invoice Id
		Long refDocId = new Long(0);
		findInvStmt.setString(1, "% = " + docRefNo + ")%");
		ResultSet rsFindInv = findInvStmt.executeQuery();
		if (rsFindInv.next())
		{
			refDocId = new Long(rsFindInv.getLong("pkid"));
		}
		// Create the doclink
		DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_ADJ,
		// ReceiptBean.TABLENAME, newRcptId,
				OfficialReceiptBean.TABLENAME, newRcptId, InvoiceBean.TABLENAME, refDocId, "MYR", bdAmount.negate(),
				"", bdAmount.negate(), "", TimeFormat.getTimestamp(), usrid);
	}

	private void issueCN(Connection topconCon, Connection jbossCon, String docRefNo, Integer usrid) throws Exception
	{
		String findInvQ =
		// "select pkid from cust_invoice_index where remarks ~* ? ";
		"select pkid from cust_invoice_index where remarks ILIKE ? ";
		PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
		String findGenQ =
		// "select pkid from acc_generic_stmt where remarks ~* ? ";
		"select pkid from acc_generic_stmt where remarks ILIKE ? ";
		PreparedStatement findGenStmt = jbossCon.prepareStatement(findGenQ);
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		String getDocInfo = "select * from cdhis where docno = ? ";
		PreparedStatement getDocInfoStmt = topconCon.prepareStatement(getDocInfo);
		String strCustId = null;
		Integer iCustId = null;
		String strDocNo = null;
		String strCurr = null;
		String strTxType = null;
		String strRemarks = "DERIVED: To Reverse " + docRefNo;
		BigDecimal bdAmount = null;
		Timestamp tsTxDate = null;
		// Get the doc info
		getDocInfoStmt.setString(1, docRefNo);
		ResultSet rsGetDocInfo = getDocInfoStmt.executeQuery();
		if (rsGetDocInfo.next())
		{
			strCustId = rsGetDocInfo.getString("custid");
			strCurr = rsGetDocInfo.getString("currid");
			strCurr = (String) hmCurr.get(new Integer(strCurr));
			strDocNo = rsGetDocInfo.getString("docno");
			strTxType = rsGetDocInfo.getString("txtype");
			tsTxDate = rsGetDocInfo.getTimestamp("txdate");
			// bdAmount = rsGetDocInfo.getBigDecimal("lcreditamt");
			bdAmount = rsGetDocInfo.getBigDecimal("ldebitamt");
			// strRemarks = rsGetDocInfo.getString("comment");
		}
		getDocInfoStmt.close();
		Log.printDebug("##### Processing Cust " + strCustId + ": DocRef = " + docRefNo + " #####");
		// Get the Customer Id
		findCustStmt.setString(1, strCustId);
		ResultSet rsFindCust = findCustStmt.executeQuery();
		if (rsFindCust.next())
		{
			iCustId = new Integer(rsFindCust.getInt("pkid"));
		}
		findCustStmt.close();
		Long refDocId = new Long(0);
		String refDocTable = null;
		// Depends on what we are reversing, could be I, DN, or RP
		if (strTxType.equals("I"))
		{
			// Get the Invoice Id
			findInvStmt.setString(1, "% = " + docRefNo + ")%");
			ResultSet rsFindInv = findInvStmt.executeQuery();
			if (rsFindInv.next())
			{
				refDocId = new Long(rsFindInv.getLong("pkid"));
				refDocTable = InvoiceBean.TABLENAME;
			}
		} else
		{
			// Get the GenericStmt Id
			findGenStmt.setString(1, "% = " + docRefNo + ")%");
			ResultSet rsFindGenStmt = findGenStmt.executeQuery();
			if (rsFindGenStmt.next())
			{
				refDocId = new Long(rsFindGenStmt.getLong("pkid"));
				refDocTable = GenericStmtBean.TABLENAME;
			}
		}
		Long newCNId = createCreditNote(iCustId, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE, GLCodeBean.CASH_DISCOUNT,
				tsTxDate, refDocTable, refDocId, docRefNo, strRemarks, TimeFormat.getTimestamp(),
				// usrid, strCurr, bdAmount);
				usrid, strDefCurr, bdAmount);
		// Create the doclink
		DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_ADJ,
				GenericStmtBean.TABLENAME, newCNId, refDocTable, refDocId, strCurr, bdAmount.negate(), "",
				bdAmount.negate(), "", TimeFormat.getTimestamp(), usrid);
	}

	private void issueNewCN(Connection topconCon, Connection jbossCon, String strCustId, BigDecimal bdAmount,
			Timestamp txDate, Integer usrid, String remarks) throws Exception
	{
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		String strRemarks = "DERIVED: Unresolved negative balance";
		Log.printDebug("##### Processing Cust " + strCustId + ": NEW CREDIT NOTE");
		// Get the Customer Id
		Integer iCustId = null;
		findCustStmt.setString(1, strCustId);
		ResultSet rsFindCust = findCustStmt.executeQuery();
		if (rsFindCust.next())
		{
			iCustId = new Integer(rsFindCust.getInt("pkid"));
		}
		findCustStmt.close();
		Long newCNId = createCreditNote(iCustId, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE, GLCodeBean.CASH_DISCOUNT,
				txDate, "", new Long(0), "", strRemarks, TimeFormat.getTimestamp(), usrid, strDefCurr, bdAmount);
	}

	private void issueDN(Connection topconCon, Connection jbossCon, String docRefNo, Integer usrid) throws Exception
	{
		String findInvQ =
		// "select pkid from cust_receipt_index where payment_remarks ~* ? ";
		"select pkid from cust_receipt_index where payment_remarks ILIKE ? ";
		PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
		String findGenQ = "select pkid from acc_generic_stmt where remarks ILIKE ? ";
		PreparedStatement findGenStmt = jbossCon.prepareStatement(findGenQ);
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		String getDocInfo = "select * from cdhis where docno = ? ";
		PreparedStatement getDocInfoStmt = topconCon.prepareStatement(getDocInfo);
		String strCustId = null;
		Integer iCustId = null;
		String strDocNo = null;
		String strCurr = null;
		String strTxType = null;
		String strRemarks = "DERIVED: To Reverse " + docRefNo;
		BigDecimal bdAmount = null;
		Timestamp tsTxDate = null;
		// Get the doc info
		getDocInfoStmt.setString(1, docRefNo);
		ResultSet rsGetDocInfo = getDocInfoStmt.executeQuery();
		if (rsGetDocInfo.next())
		{
			strCustId = rsGetDocInfo.getString("custid");
			strCurr = rsGetDocInfo.getString("currid");
			strCurr = (String) hmCurr.get(new Integer(strCurr));
			strDocNo = rsGetDocInfo.getString("docno");
			strTxType = rsGetDocInfo.getString("txtype");
			tsTxDate = rsGetDocInfo.getTimestamp("txdate");
			bdAmount = rsGetDocInfo.getBigDecimal("lcreditamt");
			// strRemarks = rsGetDocInfo.getString("comment");
		}
		if (!strCurr.equals(strDefCurr))
		{
			strRemarks += ", (CURR=" + strCurr + ")";
		}
		getDocInfoStmt.close();
		Log.printDebug("##### Processing Cust " + strCustId + ": DocRef = " + docRefNo + " #####");
		// Get the Customer Id
		findCustStmt.setString(1, strCustId);
		ResultSet rsFindCust = findCustStmt.executeQuery();
		if (rsFindCust.next())
		{
			iCustId = new Integer(rsFindCust.getInt("pkid"));
		}
		findCustStmt.close();
		Long refDocId = new Long(0);
		String refDocTable = null;
		// Depends on what we are reversing, could be C or P
		if (strTxType.equals("P"))
		{
			// Get the Receipt Id
			findInvStmt.setString(1, "% = " + docRefNo + ")%");
			ResultSet rsFindInv = findInvStmt.executeQuery();
			if (rsFindInv.next())
			{
				refDocId = new Long(rsFindInv.getLong("pkid"));
				// refDocTable = ReceiptBean.TABLENAME;
				refDocTable = OfficialReceiptBean.TABLENAME;
			}
		} else
		{
			// Get the GenericStmt Id
			findGenStmt.setString(1, "% = " + docRefNo + ")%");
			ResultSet rsFindGenStmt = findGenStmt.executeQuery();
			if (rsFindGenStmt.next())
			{
				refDocId = new Long(rsFindGenStmt.getLong("pkid"));
				refDocTable = GenericStmtBean.TABLENAME;
			}
		}
		Long newDNId = createDebitNote(iCustId, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE, GLCodeBean.INTEREST_REVENUE,
				tsTxDate, refDocTable, refDocId, docRefNo, strRemarks, TimeFormat.getTimestamp(),
				// usrid, strCurr, bdAmount);
				usrid, strDefCurr, bdAmount);
		// Create the doclink
		DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_ADJ,
				GenericStmtBean.TABLENAME, newDNId, refDocTable, refDocId, strCurr, bdAmount, "", bdAmount, "",
				TimeFormat.getTimestamp(), usrid);
	}

	private Long createCustReceipt(Integer custId, // naPkid,
			Integer caPkid, Timestamp tsReceiptDate, Timestamp tsEffFromDate, Timestamp tsEffToDate, String currency,
			BigDecimal bdPaymentAmt, String strPaymentMethod, String strPaymentRmks,
			// String strGLCodeCredit,
			// String strGLCodeDebit,
			String strChequeNumber,
			// String strCashAccount,
			Integer iUsrId, Timestamp tsCreate) throws Exception
	{
		Long salesTxnId = new Long("0");
		String strAmountStr = CurrencyFormat.toWords(bdPaymentAmt, "", "");
		strAmountStr = strAmountStr + " ONLY";
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
			naObj.remarks = strPaymentRmks;
			naObj.accPCCenterId = iDefPCCenterId;
			naObj.state = NominalAccountBean.STATE_CREATED;
			naObj.status = NominalAccountBean.STATUS_ACTIVE;
			naObj.lastUpdate = TimeFormat.getTimestamp();
			naObj.userIdUpdate = iUsrId;
			NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
		}
		// / Get objects based on parameters above
		// NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
		CashAccountObject caObj = CashAccountNut.getObject(caPkid);
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
		/*
		 * NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		 * natObj.nominalAccount = naObj.pkid; natObj.foreignTable =
		 * NominalAccountTxnBean.FT_CUST_RECEIPT; natObj.foreignKey =
		 * cRcptObj.pkid; natObj.code = "not_used"; natObj.info1 = "";
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
		 * if(natEJB == null) { try { sPayEJB.remove();} catch(Exception ex) {
		 * ex.printStackTrace();} throw new Exception("Failed to create NAT"); }
		 * /// 4) update cash account transactions CashAccTxnObject catObj = new
		 * CashAccTxnObject(); catObj.txnCode = CashAccTxnBean.TXN_CODE_DEFAULT;
		 * catObj.glCodeCredit = GLCodeBean.ACC_RECEIVABLE; catObj.glCodeDebit =
		 * caObj.accountType; catObj.personInCharge = iUsrId; catObj.accFrom =
		 * new Integer("0");// another known bank account catObj.accTo =
		 * caObj.pkId; catObj.foreignTable = CashAccTxnBean.FT_CUST_RECEIPT;
		 * catObj.foreignKey = cRcptObj.pkid; catObj.currency = naObj.currency;
		 * catObj.amount = bdPaymentAmt; catObj.txnTime = tsReceiptDate;
		 * catObj.remarks = strPaymentRmks; catObj.info1 = ""; catObj.info2 =
		 * ""; catObj.state = CashAccTxnBean.ST_CREATED; catObj.status =
		 * CashAccTxnBean.STATUS_ACTIVE; catObj.lastUpdate = tsCreate;
		 * catObj.userIdUpdate = iUsrId; catObj.pcCenter = caObj.pcCenter;
		 * CashAccTxn catEJB = CashAccTxnNut.fnCreate(catObj); /// 5) update
		 * cash account balance // no need to set the balance !!! hurray !!!!
		 * 
		 * /// 3) update the nominal account NominalAccount naEJB =
		 * NominalAccountNut.getHandle(naObj.pkid); BigDecimal bdLatestBal =
		 * naEJB.getAmount(); bdLatestBal = bdLatestBal.add(natObj.amount);
		 * naObj.amount = bdLatestBal;
		 */
		// return sPayEJB.getPkid();
		return receiptObj.pkid;
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

	private Long createReversePayment(String strStmtType,
	// Integer iNominalAccId,
			Integer iCustId, Integer iCashAccountId, String currency, BigDecimal bdPaymentAmt,
			String strChequeCreditCardNo, String strRemarks, String strInfo1, Timestamp tsDateStmt,
			String strForeignStmtTable, Long iSettleStmtId, Integer usrid) throws Exception
	{
		String strErrMsg = null;
		String strAmtInWords = CurrencyFormat.toWords(bdPaymentAmt, "", "");
		strAmtInWords = strAmtInWords + " ONLY";
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
		NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER, iDefPCCenterId,
				iCustId, "MYR");
		if (naObj == null)
		{
			naObj = new NominalAccountObject();
			// naObj.pkid = new Integer("0");
			// naObj.code = new String("not_used");
			naObj.namespace = NominalAccountBean.NS_CUSTOMER;
			naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
			naObj.foreignKey = iCustId;
			naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
			// naObj.currency = thisCurr;
			naObj.currency = "MYR";
			// naObj.amount = naObj.amount.add(lTxAmt);
			naObj.remarks = strRemarks;
			naObj.accPCCenterId = iDefPCCenterId;
			naObj.state = NominalAccountBean.STATE_CREATED;
			naObj.status = NominalAccountBean.STATUS_ACTIVE;
			naObj.lastUpdate = TimeFormat.getTimestamp();
			naObj.userIdUpdate = usrid;
			NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
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

	private void cancelInv(String docNo, Integer userId) throws Exception
	{
		String cancelSelect =
		// "select sales_txn_id from cust_invoice_index where remarks ~* '" +
		// docNo + "'";
		"select sales_txn_id from cust_invoice_index where remarks ILIKE '%" + docNo + "%'";
		Statement cancelStmt = jbossCon.createStatement();
		ResultSet rs = cancelStmt.executeQuery(cancelSelect);
		if (rs.next())
		{
			Long salesTxnId = new Long(rs.getLong("sales_txn_id"));
			// SalesTxnNut.cancelTxn(salesTxnId, userId);
		}
	}

	private void cancelGenStmt(String docNo) throws Exception
	{
		String cancelSelect =
		// "select pkid from acc_generic_stmt where remarks ~* '" + docNo + "'";
		"select pkid from acc_generic_stmt where remarks ILIKE '%" + docNo + "%'";
		Statement cancelStmt = jbossCon.createStatement();
		ResultSet rs = cancelStmt.executeQuery(cancelSelect);
		if (rs.next())
		{
			Long genStmtId = new Long(rs.getLong("pkid"));
			GenericStmtNut.cancel(genStmtId);
		}
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
 (NEW) 
 Aug 04 DB seems to have solved a few of these discrepancies in the Jul 04 DB.

 CustID solved:
 MA0014
 ME0031
 ME0033 ?? need to issue DN to reverse OR20303
 MC0103 Change CN of 765 t0 716.5
 MK0015
 SS0080
 SW0004

 (NEW) SU0048
 Action: Issue CN to reduce amount for R/OR190104

 (NEW) MS0010
 The following Invoices / DNs were invalidated by topcon, hence has to be reversed:

 docno  | txtype | ldebitamt | lcreditamt | txstatus
 ---------+--------+-----------+------------+----------
 SM_476  | I      | 4450.0000 |     0.0000 | C
 SM_745  | I      | 2300.0000 |     0.0000 | C
 VM_1731 | I      |   92.0000 |     0.0000 | C
 VM_1756 | I      |  295.0000 |     0.0000 | C
 VM_1880 | I      |  105.0000 |     0.0000 | C
 DN1880  | D      |  105.0000 |     0.0000 | C


 **/
/**
 * These are the last few fixes ...
 *  * MA0014 (OR16506 both RP and P) Action: Issue Receipt to account for
 * OR16506
 *  * MC0014 CITY OPTICAL-KL 77834904 -21370.00 (MED410 oversettled SM634 +
 * RV11782 not in payrefd) Action: Issue CN to reverse RV11782 (1130.00)
 *  * MC0103 (O/R52473 is not found in payrefd for MC0103) Action: (1) Issue
 * Receipt of 48.00 to settle VM4626 (2) Issue CN of 765.00 to make up the
 * negative balance ????? dunno why !!
 *  * ME0031 (OR14097 both RP and P) Action: Issue CN to account for OR14097
 *  * ME0033 (O/R17437 not found in cdhis) Action: Issue Receipt O/R17437* for
 * SM1117
 *  * MK0015 (OR15621 unaccounted for) Action: Issue Receipt OR15621* for SM913
 *  * MS0010 (SM476,SM745,VM1731, VM1756,VM1880 overpaid, CN1880 Credited for
 * nothing (both C and I) Action: Issue DN to reverse CM1880
 *  * SM0040 (OR8814 overpayment for nonexistant RV8814 DN) Action: Issue DN to
 * reverse OR8814
 *  * SP0046 PRAI CONSTRN.& 04399 9006 -100.00 (O/R19126 unaccounted for - not
 * found in payrefd -exception!!) Action: Issue DN to reverse O/R19126
 *  * SS0080 (O/R17435 that pays for SS10258 is DELETED!!) Action: Issue Receipt
 * for SS10258 (1250.00)
 *  * SW0004 (OR12495 paid TWICE for SS1655) Action: Issue Receipt using
 * OR12495* to close SS1655 (payrefd has dup entries)
 * 
 */
public class DoTopConFixMisc implements Action
{
	private String strClassName = "DoTopConFixMisc";
	/***************************************************************************
	 * GLOBAL VARIABLES
	 **************************************************************************/
	Integer iDefLocId = new Integer(1000);
	Integer iDefSvcCtrId = new Integer(1);
	Integer iDefPCCenterId = new Integer(1000);
	Integer iDefCondId = new Integer(StockNut.STK_COND_GOOD);
	Integer iDefCashAccId = new Integer(1000);
	Integer iDefChequeAccId = new Integer(1001);
	String strDefCurr = "MYR";
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
	private BranchObject defBranchObj = null;
	Connection con = null;
	Connection jbossCon = null;

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Get processing start time
		Timestamp tsStart = TimeFormat.getTimestamp();
		if (defBranchObj == null)
			defBranchObj = BranchNut.getObject(iDefSvcCtrId);
		/*
		 * HashMap hmCurr = new HashMap(); hmCurr.put(new Integer(1), "MYR");
		 * hmCurr.put(new Integer(2), "USD"); hmCurr.put(new Integer(3), "SGD");
		 * hmCurr.put(new Integer(4), "4"); hmCurr.put(new Integer(5), "5");
		 * hmCurr.put(new Integer(6), "6"); hmCurr.put(new Integer(7), "7");
		 */
		try
		{
			// Connection to topcon DB
			String url = "jdbc:postgresql://localhost:5432/topcon";
			con = DriverManager.getConnection(url, "jboss", "jboss");
			// Connection to Wavelet EMP DB
			String jbossURL = "jdbc:postgresql://localhost:5432/wsemp";
			jbossCon = DriverManager.getConnection(jbossURL, "jboss", "jboss");
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
			/*
			 * String findInvQ = "select pkid from cust_invoice_index where
			 * remarks ~* ? "; PreparedStatement findInvStmt =
			 * jbossCon.prepareStatement(findInvQ);
			 * 
			 * String findCustQ = "select pkid from cust_account_index where
			 * acc_code = ? "; PreparedStatement findCustStmt =
			 * jbossCon.prepareStatement(findCustQ);
			 * 
			 * String findCurrQ = "select currid from cdhis where docno = ? ";
			 * PreparedStatement findCurrStmt = con.prepareStatement(findCurrQ);
			 * 
			 * String getDocInfo = "select * from cdhis where docno = ? ";
			 * PreparedStatement getDocInfoStmt =
			 * con.prepareStatement(getDocInfo);
			 */
			/*******************************************************************
			 * CustCode: MA0014
			 * 
			 * Fix: Issue Receipt (OR16506*) for SM1042*
			 ******************************************************************/
			// issueReceipt(con, jbossCon, "OR16506", "SM1042", usrid);
			/*******************************************************************
			 * CustCode: MC0014 Fix: Issue CN to reverse RV11782 (1130.00)
			 ******************************************************************/
			issueCN(con, jbossCon, "RV11782", usrid);
			/*******************************************************************
			 * CustCode: MC0103 Fix: (1) Issue Receipt O/R52473* for Inv. VM4626
			 * (2) Issue CN to make up negative balance (-765)
			 ******************************************************************/
			issueReceipt(con, jbossCon, "O/R52473", "VM4626", usrid);
			String getDate = "select txdate from cdhis where docno = 'OR_52473'";
			Statement getDateStmt = con.createStatement();
			ResultSet rsGetDate = getDateStmt.executeQuery(getDate);
			Timestamp txDate = TimeFormat.getTimestamp();
			if (rsGetDate.next())
			{
				txDate = rsGetDate.getTimestamp("txdate");
			}
			String strRemarks = "DERIVED: Unresolved negative balance";
			// issueNewCN(con,jbossCon,"MC0103",new BigDecimal(765.00),
			issueNewCN(con, jbossCon, "MC0103", new BigDecimal(716.50), txDate, usrid, strRemarks);
			/*******************************************************************
			 * CustCode: ME0031 Fix: Issue Receipt (OR14097*) for SM1064
			 ******************************************************************/
			// issueReceipt(con, jbossCon, "OR14097", "SM1064", usrid);
			/*******************************************************************
			 * CustCode: ME0033 Fix: Issue Receipt (O/R17437*) for SM1117
			 ******************************************************************/
			// issueNewReceipt(con, jbossCon, "ME0033", "O/R17437", "SM1117",
			// usrid);
			issueDN(con, jbossCon, "OR20303", usrid);
			/*******************************************************************
			 * CustCode: MK0015 Fix: Issue Receipt (OR15621*) for SM913
			 ******************************************************************/
			// issueReceipt(con, jbossCon, "OR15621", "SM913", usrid);
			/*******************************************************************
			 * CustCode: MS0010 Fix: Issue DN to reverse CM1880
			 ******************************************************************/
			issueDN(con, jbossCon, "CM1880", usrid);
			cancelInv("SM_476", usrid);
			cancelInv("SM_745", usrid);
			cancelInv("VM_1731", usrid);
			cancelInv("VM_1756", usrid);
			cancelInv("VM_1880", usrid);
			cancelGenStmt("DN1880");
			/*******************************************************************
			 * CustCode: SM0040 Fix: Issue DN/ReversePayment to reverse OR8814
			 * for non-existant DN RV8814
			 ******************************************************************/
			issueDN(con, jbossCon, "OR8814", usrid);
			/*******************************************************************
			 * CustCode: SP0046 Fix: Issue DN/ReversePayment to reverse O/R19126
			 ******************************************************************/
			issueDN(con, jbossCon, "O/R19126", usrid);
			/*******************************************************************
			 * CustCode: SS0080 Fix: Issue Receipt O/R17435 for Inv. SS10258
			 ******************************************************************/
			// issueReceipt(con, jbossCon, "O/R17435", "SS10258", usrid);
			/*******************************************************************
			 * CustCode: SU0048 Fix: Issue Receipt OR12495 for Inv. SS1655
			 ******************************************************************/
			getDate = "select txdate from cdhis where docno = 'R/OR190104'";
			rsGetDate = getDateStmt.executeQuery(getDate);
			txDate = TimeFormat.getTimestamp();
			if (rsGetDate.next())
			{
				txDate = rsGetDate.getTimestamp("txdate");
			}
			strRemarks = "DERIVED: Correct Reverse Payment R/OR190104";
			// issueNewCN(con,jbossCon,"MC0103",new BigDecimal(765.00),
			issueNewCN(con, jbossCon, "SU0048", new BigDecimal(500), txDate, usrid, strRemarks);
			/*******************************************************************
			 * CustCode: SW0004 Fix: Issue Receipt OR12495 for Inv. SS1655
			 ******************************************************************/
			// issueReceipt(con, jbossCon, "OR12495", "SS1655", usrid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while fixing NAT: " + ex.getMessage());
		}
		Log.printVerbose("***** END: FIX MISCELLANEOUS *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "FIX MISCELLANEOUS");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		return new ActionRouter("test-migrate-topcondb-page");
	}

	private void issueReceipt(Connection topconCon, Connection jbossCon, String payNo, String docRefNo, Integer usrid)
			throws Exception
	{
		String findInvQ =
		// "select pkid from cust_invoice_index where remarks ~* ? ";
		"select pkid from cust_invoice_index where remarks ILIKE ? ";
		PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		String getDocInfo = "select * from cdhis where docno = ? ";
		PreparedStatement getDocInfoStmt = topconCon.prepareStatement(getDocInfo);
		String strCustId = null;
		Integer iCustId = null;
		String strDocNo = null;
		String strCurr = null;
		String strRemarks = "DERIVED PAYMENT (Old DocRef = " + payNo + "**)";
		BigDecimal bdAmount = null;
		Timestamp tsTxDate = null;
		// Get the doc info
		getDocInfoStmt.setString(1, payNo);
		ResultSet rsGetDocInfo = getDocInfoStmt.executeQuery();
		if (rsGetDocInfo.next())
		{
			strCustId = rsGetDocInfo.getString("custid");
			strCurr = rsGetDocInfo.getString("currid");
			strCurr = (String) hmCurr.get(new Integer(strCurr));
			strDocNo = rsGetDocInfo.getString("docno");
			tsTxDate = rsGetDocInfo.getTimestamp("txdate");
			bdAmount = rsGetDocInfo.getBigDecimal("lcreditamt");
			// strRemarks = rsGetDocInfo.getString("comment");
		}
		getDocInfoStmt.close();
		Log.printDebug("##### Processing Cust " + strCustId + ": " + payNo + " #####");
		// Get the Customer Id
		findCustStmt.setString(1, strCustId);
		ResultSet rsFindCust = findCustStmt.executeQuery();
		if (rsFindCust.next())
		{
			iCustId = new Integer(rsFindCust.getInt("pkid"));
		}
		findCustStmt.close();
		// Query for Details from CDHisD
		String txnDQuery = "select desc1 from cdhisd where docref = '" + payNo + "'";
		Statement txnDStmt = topconCon.createStatement();
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
			strRemarks = strRemarks + " (DESC=" + desc1 + ")";
		}
		txnDStmt.close();
		Long newRcptId = createCustReceipt(iCustId, iCashAccId, tsTxDate, tsTxDate, tsTxDate, strCurr, bdAmount,
				strPayMethod, strRemarks, strChequeNo, // "" for Cash
				usrid, TimeFormat.getTimestamp());
		Log.printVerbose("Successfully created Receipt " + payNo);
		// Get the Invoice Id
		Long refDocId = new Long(0);
		findInvStmt.setString(1, "% = " + docRefNo + ")%");
		ResultSet rsFindInv = findInvStmt.executeQuery();
		if (rsFindInv.next())
		{
			refDocId = new Long(rsFindInv.getLong("pkid"));
		}
		// Create the doclink
		DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_ADJ,
		// ReceiptBean.TABLENAME, newRcptId,
				OfficialReceiptBean.TABLENAME, newRcptId, InvoiceBean.TABLENAME, refDocId, strCurr, bdAmount.negate(),
				"", bdAmount.negate(), "", TimeFormat.getTimestamp(), usrid);
	}

	private void issueNewReceipt(Connection topconCon, Connection jbossCon, String strCustId, String payNo,
			String docRefNo, Integer usrid) throws Exception
	{
		String findInvQ =
		// "select pkid from cust_invoice_index where remarks ~* ? ";
		"select pkid from cust_invoice_index where remarks ILIKE ? ";
		PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		Integer iCustId = null;
		Log.printDebug("##### Processing Cust " + strCustId + ": " + payNo + " #####");
		// Get the Customer Id
		findCustStmt.setString(1, strCustId);
		ResultSet rsFindCust = findCustStmt.executeQuery();
		if (rsFindCust.next())
		{
			iCustId = new Integer(rsFindCust.getInt("pkid"));
		}
		findCustStmt.close();
		/*
		 * // Query for Details from CDHisD String txnDQuery = "select desc1
		 * from cdhisd where docref = '" + payNo + "'"; Statement txnDStmt =
		 * con.createStatement(); ResultSet rsTxnD =
		 * txnDStmt.executeQuery(txnDQuery);
		 * 
		 * String strPayMethod = "cash"; // default to cash String strChequeNo =
		 * ""; Integer iCashAccId = iDefCashAccId; //while(rsTxnD.next())
		 * if(rsTxnD.next()) { //String glId = rsTxnD.getString("glid"); String
		 * desc1 = rsTxnD.getString("desc1").trim();
		 * 
		 * if (!desc1.equals("") && !desc1.startsWith("0/")) { strPayMethod =
		 * "cheque"; strChequeNo = desc1; iCashAccId = iDefChequeAccId; }
		 * //remarks = remarks + ", (GLID=" + glId strRemarks = strRemarks + "
		 * (DESC=" + desc1 + ")"; } txnDStmt.close();
		 */
		String getPaymentInfo = "select paydate,lpayamt from payrefd where payno = '" + payNo + "'";
		Statement getPaymentInfoStmt = topconCon.createStatement();
		ResultSet rsPymtInfo = getPaymentInfoStmt.executeQuery(getPaymentInfo);
		Timestamp tsTxDate = null;
		BigDecimal bdAmount = null;
		if (rsPymtInfo.next())
		{
			tsTxDate = TimeFormat.createTimeStamp(rsPymtInfo.getString("paydate"), "MM/dd/yy HH:mm:ss");
			bdAmount = rsPymtInfo.getBigDecimal("lpayamt");
		}
		String strRemarks = "DERIVED PAYMENT (Old DocRef = " + payNo + "**)";
		Long newRcptId = createCustReceipt(iCustId, iDefCashAccId, tsTxDate, tsTxDate, tsTxDate, "MYR", bdAmount,
				"cash", strRemarks, "", // "" for Cash
				usrid, TimeFormat.getTimestamp());
		Log.printVerbose("Successfully created Receipt " + payNo);
		// Get the Invoice Id
		Long refDocId = new Long(0);
		findInvStmt.setString(1, "% = " + docRefNo + ")%");
		ResultSet rsFindInv = findInvStmt.executeQuery();
		if (rsFindInv.next())
		{
			refDocId = new Long(rsFindInv.getLong("pkid"));
		}
		// Create the doclink
		DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_ADJ,
		// ReceiptBean.TABLENAME, newRcptId,
				OfficialReceiptBean.TABLENAME, newRcptId, InvoiceBean.TABLENAME, refDocId, "MYR", bdAmount.negate(),
				"", bdAmount.negate(), "", TimeFormat.getTimestamp(), usrid);
	}

	private void issueCN(Connection topconCon, Connection jbossCon, String docRefNo, Integer usrid) throws Exception
	{
		String findInvQ =
		// "select pkid from cust_invoice_index where remarks ~* ? ";
		"select pkid from cust_invoice_index where remarks ILIKE ? ";
		PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
		String findGenQ =
		// "select pkid from acc_generic_stmt where remarks ~* ? ";
		"select pkid from acc_generic_stmt where remarks ILIKE ? ";
		PreparedStatement findGenStmt = jbossCon.prepareStatement(findGenQ);
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		String getDocInfo = "select * from cdhis where docno = ? ";
		PreparedStatement getDocInfoStmt = topconCon.prepareStatement(getDocInfo);
		String strCustId = null;
		Integer iCustId = null;
		String strDocNo = null;
		String strCurr = null;
		String strTxType = null;
		String strRemarks = "DERIVED: To Reverse " + docRefNo;
		BigDecimal bdAmount = null;
		Timestamp tsTxDate = null;
		// Get the doc info
		getDocInfoStmt.setString(1, docRefNo);
		ResultSet rsGetDocInfo = getDocInfoStmt.executeQuery();
		if (rsGetDocInfo.next())
		{
			strCustId = rsGetDocInfo.getString("custid");
			strCurr = rsGetDocInfo.getString("currid");
			strCurr = (String) hmCurr.get(new Integer(strCurr));
			strDocNo = rsGetDocInfo.getString("docno");
			strTxType = rsGetDocInfo.getString("txtype");
			tsTxDate = rsGetDocInfo.getTimestamp("txdate");
			// bdAmount = rsGetDocInfo.getBigDecimal("lcreditamt");
			bdAmount = rsGetDocInfo.getBigDecimal("ldebitamt");
			// strRemarks = rsGetDocInfo.getString("comment");
		}
		getDocInfoStmt.close();
		Log.printDebug("##### Processing Cust " + strCustId + ": DocRef = " + docRefNo + " #####");
		// Get the Customer Id
		findCustStmt.setString(1, strCustId);
		ResultSet rsFindCust = findCustStmt.executeQuery();
		if (rsFindCust.next())
		{
			iCustId = new Integer(rsFindCust.getInt("pkid"));
		}
		findCustStmt.close();
		Long refDocId = new Long(0);
		String refDocTable = null;
		// Depends on what we are reversing, could be I, DN, or RP
		if (strTxType.equals("I"))
		{
			// Get the Invoice Id
			findInvStmt.setString(1, "% = " + docRefNo + ")%");
			ResultSet rsFindInv = findInvStmt.executeQuery();
			if (rsFindInv.next())
			{
				refDocId = new Long(rsFindInv.getLong("pkid"));
				refDocTable = InvoiceBean.TABLENAME;
			}
		} else
		{
			// Get the GenericStmt Id
			findGenStmt.setString(1, "% = " + docRefNo + ")%");
			ResultSet rsFindGenStmt = findGenStmt.executeQuery();
			if (rsFindGenStmt.next())
			{
				refDocId = new Long(rsFindGenStmt.getLong("pkid"));
				refDocTable = GenericStmtBean.TABLENAME;
			}
		}
		Long newCNId = createCreditNote(iCustId, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE, GLCodeBean.CASH_DISCOUNT,
				tsTxDate, refDocTable, refDocId, docRefNo, strRemarks, TimeFormat.getTimestamp(),
				// usrid, strCurr, bdAmount);
				usrid, strDefCurr, bdAmount);
		// Create the doclink
		DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_ADJ,
				GenericStmtBean.TABLENAME, newCNId, refDocTable, refDocId, strCurr, bdAmount.negate(), "",
				bdAmount.negate(), "", TimeFormat.getTimestamp(), usrid);
	}

	private void issueNewCN(Connection topconCon, Connection jbossCon, String strCustId, BigDecimal bdAmount,
			Timestamp txDate, Integer usrid, String remarks) throws Exception
	{
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		String strRemarks = "DERIVED: Unresolved negative balance";
		Log.printDebug("##### Processing Cust " + strCustId + ": NEW CREDIT NOTE");
		// Get the Customer Id
		Integer iCustId = null;
		findCustStmt.setString(1, strCustId);
		ResultSet rsFindCust = findCustStmt.executeQuery();
		if (rsFindCust.next())
		{
			iCustId = new Integer(rsFindCust.getInt("pkid"));
		}
		findCustStmt.close();
		Long newCNId = createCreditNote(iCustId, GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE, GLCodeBean.CASH_DISCOUNT,
				txDate, "", new Long(0), "", strRemarks, TimeFormat.getTimestamp(), usrid, strDefCurr, bdAmount);
	}

	private void issueDN(Connection topconCon, Connection jbossCon, String docRefNo, Integer usrid) throws Exception
	{
		String findInvQ =
		// "select pkid from cust_receipt_index where payment_remarks ~* ? ";
		"select pkid from cust_receipt_index where payment_remarks ILIKE ? ";
		PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
		String findGenQ = "select pkid from acc_generic_stmt where remarks ILIKE ? ";
		PreparedStatement findGenStmt = jbossCon.prepareStatement(findGenQ);
		String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
		PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
		String getDocInfo = "select * from cdhis where docno = ? ";
		PreparedStatement getDocInfoStmt = topconCon.prepareStatement(getDocInfo);
		String strCustId = null;
		Integer iCustId = null;
		String strDocNo = null;
		String strCurr = null;
		String strTxType = null;
		String strRemarks = "DERIVED: To Reverse " + docRefNo;
		BigDecimal bdAmount = null;
		Timestamp tsTxDate = null;
		// Get the doc info
		getDocInfoStmt.setString(1, docRefNo);
		ResultSet rsGetDocInfo = getDocInfoStmt.executeQuery();
		if (rsGetDocInfo.next())
		{
			strCustId = rsGetDocInfo.getString("custid");
			strCurr = rsGetDocInfo.getString("currid");
			strCurr = (String) hmCurr.get(new Integer(strCurr));
			strDocNo = rsGetDocInfo.getString("docno");
			strTxType = rsGetDocInfo.getString("txtype");
			tsTxDate = rsGetDocInfo.getTimestamp("txdate");
			bdAmount = rsGetDocInfo.getBigDecimal("lcreditamt");
			// strRemarks = rsGetDocInfo.getString("comment");
		}
		if (!strCurr.equals(strDefCurr))
		{
			strRemarks += ", (CURR=" + strCurr + ")";
		}
		getDocInfoStmt.close();
		Log.printDebug("##### Processing Cust " + strCustId + ": DocRef = " + docRefNo + " #####");
		// Get the Customer Id
		findCustStmt.setString(1, strCustId);
		ResultSet rsFindCust = findCustStmt.executeQuery();
		if (rsFindCust.next())
		{
			iCustId = new Integer(rsFindCust.getInt("pkid"));
		}
		findCustStmt.close();
		Long refDocId = new Long(0);
		String refDocTable = null;
		// Depends on what we are reversing, could be C or P
		if (strTxType.equals("P"))
		{
			// Get the Receipt Id
			findInvStmt.setString(1, "% = " + docRefNo + ")%");
			ResultSet rsFindInv = findInvStmt.executeQuery();
			if (rsFindInv.next())
			{
				refDocId = new Long(rsFindInv.getLong("pkid"));
				// refDocTable = ReceiptBean.TABLENAME;
				refDocTable = OfficialReceiptBean.TABLENAME;
			}
		} else
		{
			// Get the GenericStmt Id
			findGenStmt.setString(1, "% = " + docRefNo + ")%");
			ResultSet rsFindGenStmt = findGenStmt.executeQuery();
			if (rsFindGenStmt.next())
			{
				refDocId = new Long(rsFindGenStmt.getLong("pkid"));
				refDocTable = GenericStmtBean.TABLENAME;
			}
		}
		Long newDNId = createDebitNote(iCustId, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE, GLCodeBean.INTEREST_REVENUE,
				tsTxDate, refDocTable, refDocId, docRefNo, strRemarks, TimeFormat.getTimestamp(),
				// usrid, strCurr, bdAmount);
				usrid, strDefCurr, bdAmount);
		// Create the doclink
		DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_ADJ,
				GenericStmtBean.TABLENAME, newDNId, refDocTable, refDocId, strCurr, bdAmount, "", bdAmount, "",
				TimeFormat.getTimestamp(), usrid);
	}

	private Long createCustReceipt(Integer custId, // naPkid,
			Integer caPkid, Timestamp tsReceiptDate, Timestamp tsEffFromDate, Timestamp tsEffToDate, String currency,
			BigDecimal bdPaymentAmt, String strPaymentMethod, String strPaymentRmks,
			// String strGLCodeCredit,
			// String strGLCodeDebit,
			String strChequeNumber,
			// String strCashAccount,
			Integer iUsrId, Timestamp tsCreate) throws Exception
	{
		Long salesTxnId = new Long("0");
		String strAmountStr = CurrencyFormat.toWords(bdPaymentAmt, "", "");
		strAmountStr = strAmountStr + " ONLY";
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
			naObj.remarks = strPaymentRmks;
			naObj.accPCCenterId = iDefPCCenterId;
			naObj.state = NominalAccountBean.STATE_CREATED;
			naObj.status = NominalAccountBean.STATUS_ACTIVE;
			naObj.lastUpdate = TimeFormat.getTimestamp();
			naObj.userIdUpdate = iUsrId;
			NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
		}
		// / Get objects based on parameters above
		// NominalAccountObject naObj = NominalAccountNut.getObject(naPkid);
		CashAccountObject caObj = CashAccountNut.getObject(caPkid);
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
		/*
		 * NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		 * natObj.nominalAccount = naObj.pkid; natObj.foreignTable =
		 * NominalAccountTxnBean.FT_CUST_RECEIPT; natObj.foreignKey =
		 * cRcptObj.pkid; natObj.code = "not_used"; natObj.info1 = "";
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
		 * if(natEJB == null) { try { sPayEJB.remove();} catch(Exception ex) {
		 * ex.printStackTrace();} throw new Exception("Failed to create NAT"); }
		 * /// 4) update cash account transactions CashAccTxnObject catObj = new
		 * CashAccTxnObject(); catObj.txnCode = CashAccTxnBean.TXN_CODE_DEFAULT;
		 * catObj.glCodeCredit = GLCodeBean.ACC_RECEIVABLE; catObj.glCodeDebit =
		 * caObj.accountType; catObj.personInCharge = iUsrId; catObj.accFrom =
		 * new Integer("0");// another known bank account catObj.accTo =
		 * caObj.pkId; catObj.foreignTable = CashAccTxnBean.FT_CUST_RECEIPT;
		 * catObj.foreignKey = cRcptObj.pkid; catObj.currency = naObj.currency;
		 * catObj.amount = bdPaymentAmt; catObj.txnTime = tsReceiptDate;
		 * catObj.remarks = strPaymentRmks; catObj.info1 = ""; catObj.info2 =
		 * ""; catObj.state = CashAccTxnBean.ST_CREATED; catObj.status =
		 * CashAccTxnBean.STATUS_ACTIVE; catObj.lastUpdate = tsCreate;
		 * catObj.userIdUpdate = iUsrId; catObj.pcCenter = caObj.pcCenter;
		 * CashAccTxn catEJB = CashAccTxnNut.fnCreate(catObj); /// 5) update
		 * cash account balance // no need to set the balance !!! hurray !!!!
		 * 
		 * /// 3) update the nominal account NominalAccount naEJB =
		 * NominalAccountNut.getHandle(naObj.pkid); BigDecimal bdLatestBal =
		 * naEJB.getAmount(); bdLatestBal = bdLatestBal.add(natObj.amount);
		 * naObj.amount = bdLatestBal;
		 */
		// return sPayEJB.getPkid();
		return receiptObj.pkid;
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

	private Long createReversePayment(String strStmtType,
	// Integer iNominalAccId,
			Integer iCustId, Integer iCashAccountId, String currency, BigDecimal bdPaymentAmt,
			String strChequeCreditCardNo, String strRemarks, String strInfo1, Timestamp tsDateStmt,
			String strForeignStmtTable, Long iSettleStmtId, Integer usrid) throws Exception
	{
		String strErrMsg = null;
		String strAmtInWords = CurrencyFormat.toWords(bdPaymentAmt, "", "");
		strAmtInWords = strAmtInWords + " ONLY";
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
		NominalAccountObject naObj = NominalAccountNut.getObject(NominalAccountBean.FT_CUSTOMER, iDefPCCenterId,
				iCustId, "MYR");
		if (naObj == null)
		{
			naObj = new NominalAccountObject();
			// naObj.pkid = new Integer("0");
			// naObj.code = new String("not_used");
			naObj.namespace = NominalAccountBean.NS_CUSTOMER;
			naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
			naObj.foreignKey = iCustId;
			naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
			// naObj.currency = thisCurr;
			naObj.currency = "MYR";
			// naObj.amount = naObj.amount.add(lTxAmt);
			naObj.remarks = strRemarks;
			naObj.accPCCenterId = iDefPCCenterId;
			naObj.state = NominalAccountBean.STATE_CREATED;
			naObj.status = NominalAccountBean.STATUS_ACTIVE;
			naObj.lastUpdate = TimeFormat.getTimestamp();
			naObj.userIdUpdate = usrid;
			NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
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

	private void cancelInv(String docNo, Integer userId) throws Exception
	{
		String cancelSelect =
		// "select sales_txn_id from cust_invoice_index where remarks ~* '" +
		// docNo + "'";
		"select sales_txn_id from cust_invoice_index where remarks ILIKE '%" + docNo + "%'";
		Statement cancelStmt = jbossCon.createStatement();
		ResultSet rs = cancelStmt.executeQuery(cancelSelect);
		if (rs.next())
		{
			Long salesTxnId = new Long(rs.getLong("sales_txn_id"));
			//SalesTxnNut.cancelTxn(salesTxnId, userId);
		}
	}

	private void cancelGenStmt(String docNo) throws Exception
	{
		String cancelSelect =
		// "select pkid from acc_generic_stmt where remarks ~* '" + docNo + "'";
		"select pkid from acc_generic_stmt where remarks ILIKE '%" + docNo + "%'";
		Statement cancelStmt = jbossCon.createStatement();
		ResultSet rs = cancelStmt.executeQuery(cancelSelect);
		if (rs.next())
		{
			Long genStmtId = new Long(rs.getLong("pkid"));
			GenericStmtNut.cancel(genStmtId);
		}
	}
}
