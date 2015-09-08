package com.vlee.servlet.test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import com.vlee.ejb.customer.InvoiceBean;
import com.vlee.ejb.inventory.StockNut;
import com.vlee.ejb.user.User;
import com.vlee.ejb.user.UserNut;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.CurrencyFormat;
import com.vlee.util.Log;
import com.vlee.util.Task;
import com.vlee.util.TimeFormat;

/**
 * 
 * This fix is used for: [1] Fixing invoices that have been overpaid
 */
public class DoTopConFixDupPymt implements Action
{
	private String strClassName = "DoTopConFixDupPymt";
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
			// drop all tmp tables
			String dropTmpTables = "drop table tmp_dist_payrefd, tmp_settle";
			String tmpDistPayrefD = "select distinct custid,payno,paytype,txrefno,txtype,paydate,txdate,lpayamt,ltxamt into tmp_dist_payrefd from payrefd";
			String tmpSettle = "select txrefno,sum(ltxamt) as total into tmp_settle from tmp_dist_payrefd group by txrefno";
			try
			{
				topconStmt.executeUpdate(dropTmpTables);
			} catch (Exception ex)
			{ /* continue */
			}
			;
			topconStmt.executeUpdate(tmpDistPayrefD);
			topconStmt.executeUpdate(tmpSettle);
			String queryOverPymt = "select c.custid,c.txdate,c.docno,c.currid,c.ldebitamt,c.lcreditamt,s.* from cdhis c join tmp_settle s on (c.docno = s.txrefno) where c.ldebitamt < s.total order by c.docno";
			Statement queryOverPymtStmt = con.createStatement();
			ResultSet rsOverPymt = queryOverPymtStmt.executeQuery(queryOverPymt);
			/*******************************************************************
			 * the logic:
			 * 
			 * for each overpaid invoice check if there's a diff custid from the
			 * owner of the invoice group by custid for each custid issue DN if
			 * (total DN < overpaid amount) means we need to charge
			 * (overpaidAmt-totalDNAmt) to the owner of the invoice
			 */
			String checkOtherCust = "select * from tmp_dist_payrefd where txrefno = ? and custid != ?";
			PreparedStatement checkOtherCustStmt = con.prepareStatement(checkOtherCust);
			String findInvQ =
			// "select pkid from cust_invoice_index where remarks ~* ? ";
			"select pkid from cust_invoice_index where remarks ILIKE ? ";
			PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
			String findDNQ =
			// "select pkid from acc_generic_stmt where remarks ~* ? ";
			"select pkid from acc_generic_stmt where remarks ILIKE ? ";
			PreparedStatement findDNStmt = jbossCon.prepareStatement(findDNQ);
			String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
			PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
			String findCurrQ = "select currid from cdhis where docno = ? ";
			PreparedStatement findCurrStmt = con.prepareStatement(findCurrQ);
			curTask = new Task("Fix Duplicate Settlements", rsOverPymt.getFetchSize());
			int count = 0;
			while (rsOverPymt.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printVerbose("*** Processing Txn " + ++count);
				String strTxRefNo = rsOverPymt.getString("txrefno");
				String strCustCode = rsOverPymt.getString("custid");
				String strCurrId = rsOverPymt.getString("currid");
				Timestamp tsTxDate = rsOverPymt.getTimestamp("txdate");
				BigDecimal bdLCreditAmt = rsOverPymt.getBigDecimal("lcreditamt");
				BigDecimal bdTotal = rsOverPymt.getBigDecimal("total");
				BigDecimal bdOverPaid = bdTotal.subtract(bdLCreditAmt);
				// Derive the string currency
				String strCurr = (String) hmCurr.get(new Integer(strCurrId));
				// Check if other customers are wrongly charged
				checkOtherCustStmt.setString(1, strTxRefNo);
				checkOtherCustStmt.setString(2, strCustCode);
				ResultSet rsCheckCust = checkOtherCustStmt.executeQuery();
				BigDecimal bdAmtLeft = bdOverPaid;
				while (rsCheckCust.next())
				{
					String strOtherCustCode = rsCheckCust.getString("custid");
					String strOtherPayNo = rsCheckCust.getString("payno");
					String strOtherPayType = rsCheckCust.getString("paytype");
					String strOtherTxRefNo = rsCheckCust.getString("txrefno");
					String strOtherTxType = rsCheckCust.getString("txtype");
					Timestamp tsOtherPayDate = TimeFormat.createTimeStamp(rsCheckCust.getString("paydate"),
							"MM/dd/yy HH:mm:ss");
					Timestamp tsOtherTxDate = TimeFormat.createTimeStamp(rsCheckCust.getString("txdate"),
							"MM/dd/yy HH:mm:ss");
					BigDecimal bdOtherLPayAmt = rsCheckCust.getBigDecimal("lpayamt");
					// Get the Customer Id
					Integer custId = null;
					findCustStmt.setString(1, strOtherCustCode);
					ResultSet rsFindCust = findCustStmt.executeQuery();
					if (rsFindCust.next())
					{
						custId = new Integer(rsFindCust.getInt("pkid"));
					}
					/*
					 * // Get the Currency String strCurr = null;
					 * findCurrStmt.setString(1, strOtherPayNo); ResultSet
					 * rsFindCurr = findCurrStmt.executeQuery(); if
					 * (rsFindCurr.next()) { strCurr =
					 * rsFindCurr.getString("currid"); strCurr = (String)
					 * hmCurr.get(new Integer(strCurr)); }
					 */
					// Disable txtype = R later, since already catered for
					// in DocIndex migration
					String strRemarks = "";
					if (strOtherTxType.equals("R"))
					{
						Log.printVerbose("*** Detected TxType = R, ignoring ...");
						/*
						 * strRemarks = "Reverse Payment for " + strOtherPayNo +
						 * "(Old DocRef = " + strOtherTxRefNo + ")"; // Create a
						 * ReversePayment Log.printVerbose("*** " + count + ": " +
						 * strRemarks); Long newRPId = createReversePayment(
						 * GenericStmtBean.STMT_TYPE_REVERSE_PAYMENT, custId,
						 * iDefCashAccId, strCurr, bdOtherLPayAmt, "",
						 * strRemarks, "", tsOtherTxDate, "", new Long(0),
						 * usrid); Log.printVerbose("Successfully created
						 * RevPymt ID = " + newRPId.toString());
						 */
					} else
					{
						strRemarks = "Reversing " + strOtherPayNo + " for DocRef = " + strOtherTxRefNo;
						Long refDocId = new Long(0);
						if (strOtherTxType.equals("I"))
						{
							// Get the Invoice Id
							findInvStmt.setString(1, "% = " + strOtherTxRefNo + ")%");
							ResultSet rsFindInv = findInvStmt.executeQuery();
							if (rsFindInv.next())
							{
								refDocId = new Long(rsFindInv.getLong("pkid"));
							}
						} else if (strOtherTxType.equals("D"))
						{
							// Get the DN Id
							Long dnId = new Long(0);
							findDNStmt.setString(1, "% = " + strOtherTxRefNo + ")%");
							ResultSet rsFindInv = findDNStmt.executeQuery();
							if (rsFindInv.next())
							{
								refDocId = new Long(rsFindInv.getLong("pkid"));
							}
						}
						// Create a Debit Note
						Log.printVerbose("*** " + count + ": " + strRemarks);
						Long newDNId = createDebitNote(custId, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
								GLCodeBean.INTEREST_REVENUE, tsOtherPayDate, InvoiceBean.TABLENAME, refDocId,
								strOtherPayNo, strRemarks, TimeFormat.getTimestamp(),
								// usrid, strCurr, bdOtherLPayAmt);
								usrid, strDefCurr, bdOtherLPayAmt);
						Log.printVerbose("Successfully created DN ID = " + newDNId.toString());
					}
					bdAmtLeft = bdAmtLeft.subtract(bdOtherLPayAmt);
					Log.printVerbose("Amount Left = " + bdAmtLeft.toString());
				} // while rsOtherCust.next()
				// Now that we're done for other customers,
				// Check if bdAmtLeft == 0? if not, means we debit the owner's
				// account
				String strRemarks = "Reversing Overpayment for DocRef = " + strTxRefNo;
				if (bdAmtLeft.signum() > 0)
				{
					// Get the Customer Id
					Integer custId = null;
					findCustStmt.setString(1, strCustCode);
					ResultSet rsFindCust = findCustStmt.executeQuery();
					if (rsFindCust.next())
					{
						custId = new Integer(rsFindCust.getInt("pkid"));
					}
					/*
					 * // Get the Currency String strCurr = null;
					 * findCurrStmt.setString(1, strTxRefNo); ResultSet
					 * rsFindCurr = findCurrStmt.executeQuery(); if
					 * (rsFindCurr.next()) { strCurr =
					 * rsFindCurr.getString("currid"); strCurr = (String)
					 * hmCurr.get(new Integer(strCurr)); }
					 */
					// Get the Invoice Id
					Long refDocId = new Long(0);
					findInvStmt.setString(1, "% = " + strTxRefNo + ")%");
					ResultSet rsFindInv = findInvStmt.executeQuery();
					if (rsFindInv.next())
					{
						refDocId = new Long(rsFindInv.getLong("pkid"));
					}
					// Create a Debit Note
					Log.printVerbose("*** " + count + ": " + strRemarks);
					Long newDNId = createDebitNote(custId, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
							GLCodeBean.INTEREST_REVENUE, tsTxDate, InvoiceBean.TABLENAME, refDocId, strTxRefNo,
							strRemarks, TimeFormat.getTimestamp(),
							// usrid, strCurr, bdAmtLeft);
							usrid, strDefCurr, bdAmtLeft);
					Log.printVerbose("Successfully created DN ID = " + newDNId.toString());
				} // end if bdAmtLeft > 0
				else if (bdAmtLeft.signum() < 0)
				{
					// Something's wrong!!!
					Log.printDebug("!!!!!! bdAmtLeft < 0 !!!!!!");
				}
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
		/*
		 * // Get the nominal account object NominalAccountObject naObj = null;
		 * try { naObj = NominalAccountNut.getObject( //new
		 * Integer(strNominalAcc)); iNominalAccId); } catch(Exception ex) { }
		 * 
		 * if(naObj==null) { throw new Exception("Error fetching Nominal Account
		 * Object "); }
		 */
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
}
package com.vlee.servlet.test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import com.vlee.ejb.customer.InvoiceBean;
import com.vlee.ejb.inventory.StockNut;
import com.vlee.ejb.user.User;
import com.vlee.ejb.user.UserNut;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.CurrencyFormat;
import com.vlee.util.Log;
import com.vlee.util.Task;
import com.vlee.util.TimeFormat;

/**
 * 
 * This fix is used for: [1] Fixing invoices that have been overpaid
 */
public class DoTopConFixDupPymt implements Action
{
	private String strClassName = "DoTopConFixDupPymt";
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
			// drop all tmp tables
			String dropTmpTables = "drop table tmp_dist_payrefd, tmp_settle";
			String tmpDistPayrefD = "select distinct custid,payno,paytype,txrefno,txtype,paydate,txdate,lpayamt,ltxamt into tmp_dist_payrefd from payrefd";
			String tmpSettle = "select txrefno,sum(ltxamt) as total into tmp_settle from tmp_dist_payrefd group by txrefno";
			try
			{
				topconStmt.executeUpdate(dropTmpTables);
			} catch (Exception ex)
			{ /* continue */
			}
			;
			topconStmt.executeUpdate(tmpDistPayrefD);
			topconStmt.executeUpdate(tmpSettle);
			String queryOverPymt = "select c.custid,c.txdate,c.docno,c.currid,c.ldebitamt,c.lcreditamt,s.* from cdhis c join tmp_settle s on (c.docno = s.txrefno) where c.ldebitamt < s.total order by c.docno";
			Statement queryOverPymtStmt = con.createStatement();
			ResultSet rsOverPymt = queryOverPymtStmt.executeQuery(queryOverPymt);
			/*******************************************************************
			 * the logic:
			 * 
			 * for each overpaid invoice check if there's a diff custid from the
			 * owner of the invoice group by custid for each custid issue DN if
			 * (total DN < overpaid amount) means we need to charge
			 * (overpaidAmt-totalDNAmt) to the owner of the invoice
			 */
			String checkOtherCust = "select * from tmp_dist_payrefd where txrefno = ? and custid != ?";
			PreparedStatement checkOtherCustStmt = con.prepareStatement(checkOtherCust);
			String findInvQ =
			// "select pkid from cust_invoice_index where remarks ~* ? ";
			"select pkid from cust_invoice_index where remarks ILIKE ? ";
			PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
			String findDNQ =
			// "select pkid from acc_generic_stmt where remarks ~* ? ";
			"select pkid from acc_generic_stmt where remarks ILIKE ? ";
			PreparedStatement findDNStmt = jbossCon.prepareStatement(findDNQ);
			String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
			PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
			String findCurrQ = "select currid from cdhis where docno = ? ";
			PreparedStatement findCurrStmt = con.prepareStatement(findCurrQ);
			curTask = new Task("Fix Duplicate Settlements", rsOverPymt.getFetchSize());
			int count = 0;
			while (rsOverPymt.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printVerbose("*** Processing Txn " + ++count);
				String strTxRefNo = rsOverPymt.getString("txrefno");
				String strCustCode = rsOverPymt.getString("custid");
				String strCurrId = rsOverPymt.getString("currid");
				Timestamp tsTxDate = rsOverPymt.getTimestamp("txdate");
				BigDecimal bdLCreditAmt = rsOverPymt.getBigDecimal("lcreditamt");
				BigDecimal bdTotal = rsOverPymt.getBigDecimal("total");
				BigDecimal bdOverPaid = bdTotal.subtract(bdLCreditAmt);
				// Derive the string currency
				String strCurr = (String) hmCurr.get(new Integer(strCurrId));
				// Check if other customers are wrongly charged
				checkOtherCustStmt.setString(1, strTxRefNo);
				checkOtherCustStmt.setString(2, strCustCode);
				ResultSet rsCheckCust = checkOtherCustStmt.executeQuery();
				BigDecimal bdAmtLeft = bdOverPaid;
				while (rsCheckCust.next())
				{
					String strOtherCustCode = rsCheckCust.getString("custid");
					String strOtherPayNo = rsCheckCust.getString("payno");
					String strOtherPayType = rsCheckCust.getString("paytype");
					String strOtherTxRefNo = rsCheckCust.getString("txrefno");
					String strOtherTxType = rsCheckCust.getString("txtype");
					Timestamp tsOtherPayDate = TimeFormat.createTimeStamp(rsCheckCust.getString("paydate"),
							"MM/dd/yy HH:mm:ss");
					Timestamp tsOtherTxDate = TimeFormat.createTimeStamp(rsCheckCust.getString("txdate"),
							"MM/dd/yy HH:mm:ss");
					BigDecimal bdOtherLPayAmt = rsCheckCust.getBigDecimal("lpayamt");
					// Get the Customer Id
					Integer custId = null;
					findCustStmt.setString(1, strOtherCustCode);
					ResultSet rsFindCust = findCustStmt.executeQuery();
					if (rsFindCust.next())
					{
						custId = new Integer(rsFindCust.getInt("pkid"));
					}
					/*
					 * // Get the Currency String strCurr = null;
					 * findCurrStmt.setString(1, strOtherPayNo); ResultSet
					 * rsFindCurr = findCurrStmt.executeQuery(); if
					 * (rsFindCurr.next()) { strCurr =
					 * rsFindCurr.getString("currid"); strCurr = (String)
					 * hmCurr.get(new Integer(strCurr)); }
					 */
					// Disable txtype = R later, since already catered for
					// in DocIndex migration
					String strRemarks = "";
					if (strOtherTxType.equals("R"))
					{
						Log.printVerbose("*** Detected TxType = R, ignoring ...");
						/*
						 * strRemarks = "Reverse Payment for " + strOtherPayNo +
						 * "(Old DocRef = " + strOtherTxRefNo + ")"; // Create a
						 * ReversePayment Log.printVerbose("*** " + count + ": " +
						 * strRemarks); Long newRPId = createReversePayment(
						 * GenericStmtBean.STMT_TYPE_REVERSE_PAYMENT, custId,
						 * iDefCashAccId, strCurr, bdOtherLPayAmt, "",
						 * strRemarks, "", tsOtherTxDate, "", new Long(0),
						 * usrid); Log.printVerbose("Successfully created
						 * RevPymt ID = " + newRPId.toString());
						 */
					} else
					{
						strRemarks = "Reversing " + strOtherPayNo + " for DocRef = " + strOtherTxRefNo;
						Long refDocId = new Long(0);
						if (strOtherTxType.equals("I"))
						{
							// Get the Invoice Id
							findInvStmt.setString(1, "% = " + strOtherTxRefNo + ")%");
							ResultSet rsFindInv = findInvStmt.executeQuery();
							if (rsFindInv.next())
							{
								refDocId = new Long(rsFindInv.getLong("pkid"));
							}
						} else if (strOtherTxType.equals("D"))
						{
							// Get the DN Id
							Long dnId = new Long(0);
							findDNStmt.setString(1, "% = " + strOtherTxRefNo + ")%");
							ResultSet rsFindInv = findDNStmt.executeQuery();
							if (rsFindInv.next())
							{
								refDocId = new Long(rsFindInv.getLong("pkid"));
							}
						}
						// Create a Debit Note
						Log.printVerbose("*** " + count + ": " + strRemarks);
						Long newDNId = createDebitNote(custId, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
								GLCodeBean.INTEREST_REVENUE, tsOtherPayDate, InvoiceBean.TABLENAME, refDocId,
								strOtherPayNo, strRemarks, TimeFormat.getTimestamp(),
								// usrid, strCurr, bdOtherLPayAmt);
								usrid, strDefCurr, bdOtherLPayAmt);
						Log.printVerbose("Successfully created DN ID = " + newDNId.toString());
					}
					bdAmtLeft = bdAmtLeft.subtract(bdOtherLPayAmt);
					Log.printVerbose("Amount Left = " + bdAmtLeft.toString());
				} // while rsOtherCust.next()
				// Now that we're done for other customers,
				// Check if bdAmtLeft == 0? if not, means we debit the owner's
				// account
				String strRemarks = "Reversing Overpayment for DocRef = " + strTxRefNo;
				if (bdAmtLeft.signum() > 0)
				{
					// Get the Customer Id
					Integer custId = null;
					findCustStmt.setString(1, strCustCode);
					ResultSet rsFindCust = findCustStmt.executeQuery();
					if (rsFindCust.next())
					{
						custId = new Integer(rsFindCust.getInt("pkid"));
					}
					/*
					 * // Get the Currency String strCurr = null;
					 * findCurrStmt.setString(1, strTxRefNo); ResultSet
					 * rsFindCurr = findCurrStmt.executeQuery(); if
					 * (rsFindCurr.next()) { strCurr =
					 * rsFindCurr.getString("currid"); strCurr = (String)
					 * hmCurr.get(new Integer(strCurr)); }
					 */
					// Get the Invoice Id
					Long refDocId = new Long(0);
					findInvStmt.setString(1, "% = " + strTxRefNo + ")%");
					ResultSet rsFindInv = findInvStmt.executeQuery();
					if (rsFindInv.next())
					{
						refDocId = new Long(rsFindInv.getLong("pkid"));
					}
					// Create a Debit Note
					Log.printVerbose("*** " + count + ": " + strRemarks);
					Long newDNId = createDebitNote(custId, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
							GLCodeBean.INTEREST_REVENUE, tsTxDate, InvoiceBean.TABLENAME, refDocId, strTxRefNo,
							strRemarks, TimeFormat.getTimestamp(),
							// usrid, strCurr, bdAmtLeft);
							usrid, strDefCurr, bdAmtLeft);
					Log.printVerbose("Successfully created DN ID = " + newDNId.toString());
				} // end if bdAmtLeft > 0
				else if (bdAmtLeft.signum() < 0)
				{
					// Something's wrong!!!
					Log.printDebug("!!!!!! bdAmtLeft < 0 !!!!!!");
				}
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
		/*
		 * // Get the nominal account object NominalAccountObject naObj = null;
		 * try { naObj = NominalAccountNut.getObject( //new
		 * Integer(strNominalAcc)); iNominalAccId); } catch(Exception ex) { }
		 * 
		 * if(naObj==null) { throw new Exception("Error fetching Nominal Account
		 * Object "); }
		 */
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
 * This fix is used for: [1] Fixing invoices that have been overpaid
 */
public class DoTopConFixDupPymt implements Action
{
	private String strClassName = "DoTopConFixDupPymt";
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
			// drop all tmp tables
			String dropTmpTables = "drop table tmp_dist_payrefd, tmp_settle";
			String tmpDistPayrefD = "select distinct custid,payno,paytype,txrefno,txtype,paydate,txdate,lpayamt,ltxamt into tmp_dist_payrefd from payrefd";
			String tmpSettle = "select txrefno,sum(ltxamt) as total into tmp_settle from tmp_dist_payrefd group by txrefno";
			try
			{
				topconStmt.executeUpdate(dropTmpTables);
			} catch (Exception ex)
			{ /* continue */
			}
			;
			topconStmt.executeUpdate(tmpDistPayrefD);
			topconStmt.executeUpdate(tmpSettle);
			String queryOverPymt = "select c.custid,c.txdate,c.docno,c.currid,c.ldebitamt,c.lcreditamt,s.* from cdhis c join tmp_settle s on (c.docno = s.txrefno) where c.ldebitamt < s.total order by c.docno";
			Statement queryOverPymtStmt = con.createStatement();
			ResultSet rsOverPymt = queryOverPymtStmt.executeQuery(queryOverPymt);
			/*******************************************************************
			 * the logic:
			 * 
			 * for each overpaid invoice check if there's a diff custid from the
			 * owner of the invoice group by custid for each custid issue DN if
			 * (total DN < overpaid amount) means we need to charge
			 * (overpaidAmt-totalDNAmt) to the owner of the invoice
			 */
			String checkOtherCust = "select * from tmp_dist_payrefd where txrefno = ? and custid != ?";
			PreparedStatement checkOtherCustStmt = con.prepareStatement(checkOtherCust);
			String findInvQ =
			// "select pkid from cust_invoice_index where remarks ~* ? ";
			"select pkid from cust_invoice_index where remarks ILIKE ? ";
			PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
			String findDNQ =
			// "select pkid from acc_generic_stmt where remarks ~* ? ";
			"select pkid from acc_generic_stmt where remarks ILIKE ? ";
			PreparedStatement findDNStmt = jbossCon.prepareStatement(findDNQ);
			String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
			PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
			String findCurrQ = "select currid from cdhis where docno = ? ";
			PreparedStatement findCurrStmt = con.prepareStatement(findCurrQ);
			curTask = new Task("Fix Duplicate Settlements", rsOverPymt.getFetchSize());
			int count = 0;
			while (rsOverPymt.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printVerbose("*** Processing Txn " + ++count);
				String strTxRefNo = rsOverPymt.getString("txrefno");
				String strCustCode = rsOverPymt.getString("custid");
				String strCurrId = rsOverPymt.getString("currid");
				Timestamp tsTxDate = rsOverPymt.getTimestamp("txdate");
				BigDecimal bdLCreditAmt = rsOverPymt.getBigDecimal("lcreditamt");
				BigDecimal bdTotal = rsOverPymt.getBigDecimal("total");
				BigDecimal bdOverPaid = bdTotal.subtract(bdLCreditAmt);
				// Derive the string currency
				String strCurr = (String) hmCurr.get(new Integer(strCurrId));
				// Check if other customers are wrongly charged
				checkOtherCustStmt.setString(1, strTxRefNo);
				checkOtherCustStmt.setString(2, strCustCode);
				ResultSet rsCheckCust = checkOtherCustStmt.executeQuery();
				BigDecimal bdAmtLeft = bdOverPaid;
				while (rsCheckCust.next())
				{
					String strOtherCustCode = rsCheckCust.getString("custid");
					String strOtherPayNo = rsCheckCust.getString("payno");
					String strOtherPayType = rsCheckCust.getString("paytype");
					String strOtherTxRefNo = rsCheckCust.getString("txrefno");
					String strOtherTxType = rsCheckCust.getString("txtype");
					Timestamp tsOtherPayDate = TimeFormat.createTimeStamp(rsCheckCust.getString("paydate"),
							"MM/dd/yy HH:mm:ss");
					Timestamp tsOtherTxDate = TimeFormat.createTimeStamp(rsCheckCust.getString("txdate"),
							"MM/dd/yy HH:mm:ss");
					BigDecimal bdOtherLPayAmt = rsCheckCust.getBigDecimal("lpayamt");
					// Get the Customer Id
					Integer custId = null;
					findCustStmt.setString(1, strOtherCustCode);
					ResultSet rsFindCust = findCustStmt.executeQuery();
					if (rsFindCust.next())
					{
						custId = new Integer(rsFindCust.getInt("pkid"));
					}
					/*
					 * // Get the Currency String strCurr = null;
					 * findCurrStmt.setString(1, strOtherPayNo); ResultSet
					 * rsFindCurr = findCurrStmt.executeQuery(); if
					 * (rsFindCurr.next()) { strCurr =
					 * rsFindCurr.getString("currid"); strCurr = (String)
					 * hmCurr.get(new Integer(strCurr)); }
					 */
					// TODO: Disable txtype = R later, since already catered for
					// in DocIndex migration
					String strRemarks = "";
					if (strOtherTxType.equals("R"))
					{
						Log.printVerbose("*** Detected TxType = R, ignoring ...");
						/*
						 * strRemarks = "Reverse Payment for " + strOtherPayNo +
						 * "(Old DocRef = " + strOtherTxRefNo + ")"; // Create a
						 * ReversePayment Log.printVerbose("*** " + count + ": " +
						 * strRemarks); Long newRPId = createReversePayment(
						 * GenericStmtBean.STMT_TYPE_REVERSE_PAYMENT, custId,
						 * iDefCashAccId, strCurr, bdOtherLPayAmt, "",
						 * strRemarks, "", tsOtherTxDate, "", new Long(0),
						 * usrid); Log.printVerbose("Successfully created
						 * RevPymt ID = " + newRPId.toString());
						 */
					} else
					{
						strRemarks = "Reversing " + strOtherPayNo + " for DocRef = " + strOtherTxRefNo;
						Long refDocId = new Long(0);
						if (strOtherTxType.equals("I"))
						{
							// Get the Invoice Id
							findInvStmt.setString(1, "% = " + strOtherTxRefNo + ")%");
							ResultSet rsFindInv = findInvStmt.executeQuery();
							if (rsFindInv.next())
							{
								refDocId = new Long(rsFindInv.getLong("pkid"));
							}
						} else if (strOtherTxType.equals("D"))
						{
							// Get the DN Id
							Long dnId = new Long(0);
							findDNStmt.setString(1, "% = " + strOtherTxRefNo + ")%");
							ResultSet rsFindInv = findDNStmt.executeQuery();
							if (rsFindInv.next())
							{
								refDocId = new Long(rsFindInv.getLong("pkid"));
							}
						}
						// Create a Debit Note
						Log.printVerbose("*** " + count + ": " + strRemarks);
						Long newDNId = createDebitNote(custId, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
								GLCodeBean.INTEREST_REVENUE, tsOtherPayDate, InvoiceBean.TABLENAME, refDocId,
								strOtherPayNo, strRemarks, TimeFormat.getTimestamp(),
								// usrid, strCurr, bdOtherLPayAmt);
								usrid, strDefCurr, bdOtherLPayAmt);
						Log.printVerbose("Successfully created DN ID = " + newDNId.toString());
					}
					bdAmtLeft = bdAmtLeft.subtract(bdOtherLPayAmt);
					Log.printVerbose("Amount Left = " + bdAmtLeft.toString());
				} // while rsOtherCust.next()
				// Now that we're done for other customers,
				// Check if bdAmtLeft == 0? if not, means we debit the owner's
				// account
				String strRemarks = "Reversing Overpayment for DocRef = " + strTxRefNo;
				if (bdAmtLeft.signum() > 0)
				{
					// Get the Customer Id
					Integer custId = null;
					findCustStmt.setString(1, strCustCode);
					ResultSet rsFindCust = findCustStmt.executeQuery();
					if (rsFindCust.next())
					{
						custId = new Integer(rsFindCust.getInt("pkid"));
					}
					/*
					 * // Get the Currency String strCurr = null;
					 * findCurrStmt.setString(1, strTxRefNo); ResultSet
					 * rsFindCurr = findCurrStmt.executeQuery(); if
					 * (rsFindCurr.next()) { strCurr =
					 * rsFindCurr.getString("currid"); strCurr = (String)
					 * hmCurr.get(new Integer(strCurr)); }
					 */
					// Get the Invoice Id
					Long refDocId = new Long(0);
					findInvStmt.setString(1, "% = " + strTxRefNo + ")%");
					ResultSet rsFindInv = findInvStmt.executeQuery();
					if (rsFindInv.next())
					{
						refDocId = new Long(rsFindInv.getLong("pkid"));
					}
					// Create a Debit Note
					Log.printVerbose("*** " + count + ": " + strRemarks);
					Long newDNId = createDebitNote(custId, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
							GLCodeBean.INTEREST_REVENUE, tsTxDate, InvoiceBean.TABLENAME, refDocId, strTxRefNo,
							strRemarks, TimeFormat.getTimestamp(),
							// usrid, strCurr, bdAmtLeft);
							usrid, strDefCurr, bdAmtLeft);
					Log.printVerbose("Successfully created DN ID = " + newDNId.toString());
				} // end if bdAmtLeft > 0
				else if (bdAmtLeft.signum() < 0)
				{
					// Something's wrong!!!
					Log.printDebug("!!!!!! bdAmtLeft < 0 !!!!!!");
				}
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
		/*
		 * // Get the nominal account object NominalAccountObject naObj = null;
		 * try { naObj = NominalAccountNut.getObject( //new
		 * Integer(strNominalAcc)); iNominalAccId); } catch(Exception ex) { }
		 * 
		 * if(naObj==null) { throw new Exception("Error fetching Nominal Account
		 * Object "); }
		 */
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
}
package com.vlee.servlet.test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import com.vlee.ejb.customer.InvoiceBean;
import com.vlee.ejb.inventory.StockNut;
import com.vlee.ejb.user.User;
import com.vlee.ejb.user.UserNut;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.CurrencyFormat;
import com.vlee.util.Log;
import com.vlee.util.Task;
import com.vlee.util.TimeFormat;

/**
 * 
 * This fix is used for: [1] Fixing invoices that have been overpaid
 */
public class DoTopConFixDupPymt implements Action
{
	private String strClassName = "DoTopConFixDupPymt";
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
			// drop all tmp tables
			String dropTmpTables = "drop table tmp_dist_payrefd, tmp_settle";
			String tmpDistPayrefD = "select distinct custid,payno,paytype,txrefno,txtype,paydate,txdate,lpayamt,ltxamt into tmp_dist_payrefd from payrefd";
			String tmpSettle = "select txrefno,sum(ltxamt) as total into tmp_settle from tmp_dist_payrefd group by txrefno";
			try
			{
				topconStmt.executeUpdate(dropTmpTables);
			} catch (Exception ex)
			{ /* continue */
			}
			;
			topconStmt.executeUpdate(tmpDistPayrefD);
			topconStmt.executeUpdate(tmpSettle);
			String queryOverPymt = "select c.custid,c.txdate,c.docno,c.currid,c.ldebitamt,c.lcreditamt,s.* from cdhis c join tmp_settle s on (c.docno = s.txrefno) where c.ldebitamt < s.total order by c.docno";
			Statement queryOverPymtStmt = con.createStatement();
			ResultSet rsOverPymt = queryOverPymtStmt.executeQuery(queryOverPymt);
			/*******************************************************************
			 * the logic:
			 * 
			 * for each overpaid invoice check if there's a diff custid from the
			 * owner of the invoice group by custid for each custid issue DN if
			 * (total DN < overpaid amount) means we need to charge
			 * (overpaidAmt-totalDNAmt) to the owner of the invoice
			 */
			String checkOtherCust = "select * from tmp_dist_payrefd where txrefno = ? and custid != ?";
			PreparedStatement checkOtherCustStmt = con.prepareStatement(checkOtherCust);
			String findInvQ =
			// "select pkid from cust_invoice_index where remarks ~* ? ";
			"select pkid from cust_invoice_index where remarks ILIKE ? ";
			PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
			String findDNQ =
			// "select pkid from acc_generic_stmt where remarks ~* ? ";
			"select pkid from acc_generic_stmt where remarks ILIKE ? ";
			PreparedStatement findDNStmt = jbossCon.prepareStatement(findDNQ);
			String findCustQ = "select pkid from cust_account_index where acc_code = ? ";
			PreparedStatement findCustStmt = jbossCon.prepareStatement(findCustQ);
			String findCurrQ = "select currid from cdhis where docno = ? ";
			PreparedStatement findCurrStmt = con.prepareStatement(findCurrQ);
			curTask = new Task("Fix Duplicate Settlements", rsOverPymt.getFetchSize());
			int count = 0;
			while (rsOverPymt.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printVerbose("*** Processing Txn " + ++count);
				String strTxRefNo = rsOverPymt.getString("txrefno");
				String strCustCode = rsOverPymt.getString("custid");
				String strCurrId = rsOverPymt.getString("currid");
				Timestamp tsTxDate = rsOverPymt.getTimestamp("txdate");
				BigDecimal bdLCreditAmt = rsOverPymt.getBigDecimal("lcreditamt");
				BigDecimal bdTotal = rsOverPymt.getBigDecimal("total");
				BigDecimal bdOverPaid = bdTotal.subtract(bdLCreditAmt);
				// Derive the string currency
				String strCurr = (String) hmCurr.get(new Integer(strCurrId));
				// Check if other customers are wrongly charged
				checkOtherCustStmt.setString(1, strTxRefNo);
				checkOtherCustStmt.setString(2, strCustCode);
				ResultSet rsCheckCust = checkOtherCustStmt.executeQuery();
				BigDecimal bdAmtLeft = bdOverPaid;
				while (rsCheckCust.next())
				{
					String strOtherCustCode = rsCheckCust.getString("custid");
					String strOtherPayNo = rsCheckCust.getString("payno");
					String strOtherPayType = rsCheckCust.getString("paytype");
					String strOtherTxRefNo = rsCheckCust.getString("txrefno");
					String strOtherTxType = rsCheckCust.getString("txtype");
					Timestamp tsOtherPayDate = TimeFormat.createTimeStamp(rsCheckCust.getString("paydate"),
							"MM/dd/yy HH:mm:ss");
					Timestamp tsOtherTxDate = TimeFormat.createTimeStamp(rsCheckCust.getString("txdate"),
							"MM/dd/yy HH:mm:ss");
					BigDecimal bdOtherLPayAmt = rsCheckCust.getBigDecimal("lpayamt");
					// Get the Customer Id
					Integer custId = null;
					findCustStmt.setString(1, strOtherCustCode);
					ResultSet rsFindCust = findCustStmt.executeQuery();
					if (rsFindCust.next())
					{
						custId = new Integer(rsFindCust.getInt("pkid"));
					}
					/*
					 * // Get the Currency String strCurr = null;
					 * findCurrStmt.setString(1, strOtherPayNo); ResultSet
					 * rsFindCurr = findCurrStmt.executeQuery(); if
					 * (rsFindCurr.next()) { strCurr =
					 * rsFindCurr.getString("currid"); strCurr = (String)
					 * hmCurr.get(new Integer(strCurr)); }
					 */
					// Disable txtype = R later, since already catered for
					// in DocIndex migration
					String strRemarks = "";
					if (strOtherTxType.equals("R"))
					{
						Log.printVerbose("*** Detected TxType = R, ignoring ...");
						/*
						 * strRemarks = "Reverse Payment for " + strOtherPayNo +
						 * "(Old DocRef = " + strOtherTxRefNo + ")"; // Create a
						 * ReversePayment Log.printVerbose("*** " + count + ": " +
						 * strRemarks); Long newRPId = createReversePayment(
						 * GenericStmtBean.STMT_TYPE_REVERSE_PAYMENT, custId,
						 * iDefCashAccId, strCurr, bdOtherLPayAmt, "",
						 * strRemarks, "", tsOtherTxDate, "", new Long(0),
						 * usrid); Log.printVerbose("Successfully created
						 * RevPymt ID = " + newRPId.toString());
						 */
					} else
					{
						strRemarks = "Reversing " + strOtherPayNo + " for DocRef = " + strOtherTxRefNo;
						Long refDocId = new Long(0);
						if (strOtherTxType.equals("I"))
						{
							// Get the Invoice Id
							findInvStmt.setString(1, "% = " + strOtherTxRefNo + ")%");
							ResultSet rsFindInv = findInvStmt.executeQuery();
							if (rsFindInv.next())
							{
								refDocId = new Long(rsFindInv.getLong("pkid"));
							}
						} else if (strOtherTxType.equals("D"))
						{
							// Get the DN Id
							Long dnId = new Long(0);
							findDNStmt.setString(1, "% = " + strOtherTxRefNo + ")%");
							ResultSet rsFindInv = findDNStmt.executeQuery();
							if (rsFindInv.next())
							{
								refDocId = new Long(rsFindInv.getLong("pkid"));
							}
						}
						// Create a Debit Note
						Log.printVerbose("*** " + count + ": " + strRemarks);
						Long newDNId = createDebitNote(custId, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
								GLCodeBean.INTEREST_REVENUE, tsOtherPayDate, InvoiceBean.TABLENAME, refDocId,
								strOtherPayNo, strRemarks, TimeFormat.getTimestamp(),
								// usrid, strCurr, bdOtherLPayAmt);
								usrid, strDefCurr, bdOtherLPayAmt);
						Log.printVerbose("Successfully created DN ID = " + newDNId.toString());
					}
					bdAmtLeft = bdAmtLeft.subtract(bdOtherLPayAmt);
					Log.printVerbose("Amount Left = " + bdAmtLeft.toString());
				} // while rsOtherCust.next()
				// Now that we're done for other customers,
				// Check if bdAmtLeft == 0? if not, means we debit the owner's
				// account
				String strRemarks = "Reversing Overpayment for DocRef = " + strTxRefNo;
				if (bdAmtLeft.signum() > 0)
				{
					// Get the Customer Id
					Integer custId = null;
					findCustStmt.setString(1, strCustCode);
					ResultSet rsFindCust = findCustStmt.executeQuery();
					if (rsFindCust.next())
					{
						custId = new Integer(rsFindCust.getInt("pkid"));
					}
					/*
					 * // Get the Currency String strCurr = null;
					 * findCurrStmt.setString(1, strTxRefNo); ResultSet
					 * rsFindCurr = findCurrStmt.executeQuery(); if
					 * (rsFindCurr.next()) { strCurr =
					 * rsFindCurr.getString("currid"); strCurr = (String)
					 * hmCurr.get(new Integer(strCurr)); }
					 */
					// Get the Invoice Id
					Long refDocId = new Long(0);
					findInvStmt.setString(1, "% = " + strTxRefNo + ")%");
					ResultSet rsFindInv = findInvStmt.executeQuery();
					if (rsFindInv.next())
					{
						refDocId = new Long(rsFindInv.getLong("pkid"));
					}
					// Create a Debit Note
					Log.printVerbose("*** " + count + ": " + strRemarks);
					Long newDNId = createDebitNote(custId, GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE,
							GLCodeBean.INTEREST_REVENUE, tsTxDate, InvoiceBean.TABLENAME, refDocId, strTxRefNo,
							strRemarks, TimeFormat.getTimestamp(),
							// usrid, strCurr, bdAmtLeft);
							usrid, strDefCurr, bdAmtLeft);
					Log.printVerbose("Successfully created DN ID = " + newDNId.toString());
				} // end if bdAmtLeft > 0
				else if (bdAmtLeft.signum() < 0)
				{
					// Something's wrong!!!
					Log.printDebug("!!!!!! bdAmtLeft < 0 !!!!!!");
				}
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
		/*
		 * // Get the nominal account object NominalAccountObject naObj = null;
		 * try { naObj = NominalAccountNut.getObject( //new
		 * Integer(strNominalAcc)); iNominalAccId); } catch(Exception ex) { }
		 * 
		 * if(naObj==null) { throw new Exception("Error fetching Nominal Account
		 * Object "); }
		 */
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
}
