package com.vlee.servlet.test;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.math.BigDecimal;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;

public class DoDocLink implements Action
{
	private String strClassName = "DoDocLink";
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
		Log.printVerbose("***** BEGIN: DOCLINK *****");
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
			 * // 4. Perform Document Linking
			 ******************************************************************/
			// Clear the doclink table
			String clearDocLink = "delete from acc_doclink";
			Statement jbossStmt = jbossCon.createStatement();
			jbossStmt.executeUpdate(clearDocLink);
			String resetInvOutstandingBal = "update cust_invoice_index set outstanding_amt = total_amt";
			jbossStmt.executeUpdate(resetInvOutstandingBal);
			// Integer offset = new Integer(1059);
			// Integer limit = new Integer(1000);
			Integer offset = null;
			Integer limit = null;
			String doc2DocQ = "select * from payrefd";
			if (offset != null)
				doc2DocQ += " offset " + offset.intValue();
			if (limit != null)
				doc2DocQ += " limit " + limit.intValue();
			Statement doc2DocStmt = con.createStatement();
			ResultSet rsDoc2Doc = doc2DocStmt.executeQuery(doc2DocQ);
			/*
			 * // There are 6 distinct relationships between docs: topcon=#
			 * select distinct paytype,txtype from payrefd; paytype | txtype
			 * ---------+-------- C | D C | I C | R P | D P | I P | R
			 * 
			 * where
			 * C=CreditNote,D=DebitNote,I=Invoice,P=Payment,R=ReversePayment
			 */
			String ifDupQ = "select pkid from acc_doclink where " + "src_docref = ? and src_docid = ? and "
					+ "tgt_docref = ? and tgt_docid = ?";
			PreparedStatement ifDupStmt = jbossCon.prepareStatement(ifDupQ);
			String findInvQ = "select pkid from cust_invoice_index where remarks LIKE ?";
			PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
			String findCNQ = "select pkid from acc_generic_stmt where remarks LIKE ?" + " and stmt_type = '"
					+ GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE + "'";
			PreparedStatement findCNStmt = jbossCon.prepareStatement(findCNQ);
			String findDNQ = "select pkid from acc_generic_stmt where remarks LIKE ?" + " and stmt_type = '"
					+ GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE + "'";
			PreparedStatement findDNStmt = jbossCon.prepareStatement(findDNQ);
			/*
			 * String findRecQ = "select pkid from cust_receipt_index " + "where
			 * payment_remarks ~ ? and cust_account = ?"; PreparedStatement
			 * findRecStmt = jbossCon.prepareStatement(findRecQ);
			 */
			String findRecQ = "select pkid from acc_receipt_index " + "where payment_remarks LIKE ? and entity_key = ?";
			PreparedStatement findRecStmt = jbossCon.prepareStatement(findRecQ);
			String findRPayQ = "select pkid from acc_generic_stmt where remarks LIKE ?" + " and stmt_type = '"
					+ GenericStmtBean.STMT_TYPE_REVERSE_PAYMENT + "'";
			PreparedStatement findRPayStmt = jbossCon.prepareStatement(findRPayQ);
			curTask = new Task("Perform DocLink", rsDoc2Doc.getFetchSize());
			int count = (offset == null) ? 0 : offset.intValue();
			while (rsDoc2Doc.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				// Log.printVerbose("Processing Txn " + ++count);
				Log.printDebug("Processing Txn " + ++count);
				String payNo = rsDoc2Doc.getString("payno");
				String payType = rsDoc2Doc.getString("paytype");
				String payInfo = rsDoc2Doc.getString("payinfo");
				BigDecimal payRefDExchRate = rsDoc2Doc.getBigDecimal("exch_rate");
				String payRefDTxRefNo = rsDoc2Doc.getString("txrefno");
				String payRefDTxInfo = rsDoc2Doc.getString("txinfo");
				String payRefTxType = rsDoc2Doc.getString("txtype");
				BigDecimal payRefDTxExchRate = rsDoc2Doc.getBigDecimal("txexch_rate");
				Timestamp payRefDTxDate = TimeFormat
						.createTimeStamp(rsDoc2Doc.getString("txdate"), "MM/dd/yy HH:mm:ss");
				Timestamp payDate = TimeFormat.createTimeStamp(rsDoc2Doc.getString("paydate"), "MM/dd/yy HH:mm:ss");
				BigDecimal payAmt = rsDoc2Doc.getBigDecimal("payamt");
				BigDecimal lPayAmt = rsDoc2Doc.getBigDecimal("lpayamt");
				BigDecimal payRefDLTxAmt = rsDoc2Doc.getBigDecimal("ltxamt");
				String posted = rsDoc2Doc.getString("posted");
				String mthEnd = rsDoc2Doc.getString("mthend");
				String payRefDCustId = rsDoc2Doc.getString("custId");
				String srcTable = null;
				String tgtTable = null;
				Long srcId = null;
				Long tgtId = null;
				String relType = null;
				String findPayNo = "% = " + payNo + ")%";
				String findTxRefNo = "% = " + payRefDTxRefNo + ")%";
				// SOURCE = Payment (P)
				if (payType.equals("P"))
				{
					// Find the custAccId given custCode
					CustAccount thisCustAcc = CustAccountNut.getObjectByCode(payRefDCustId);
					if (thisCustAcc == null)
					{
						Log.printDebug(payRefDCustId + ": Customer Code not found!!");
						continue;
					}
					findRecStmt.setString(1, findPayNo);
					findRecStmt.setInt(2, thisCustAcc.getPkid().intValue());
					ResultSet rsFindRec = findRecStmt.executeQuery();
					if (rsFindRec.getFetchSize() > 2) // Could have duplicates
					{
						// This shouldn't happen, shows that the query is not
						// unique
						Log.printDebug("*** Receipt: MORE THAN TWO ROWS FOUND!! ***");
					}
					if (rsFindRec.next())
					{
						srcId = new Long(rsFindRec.getLong("pkid"));
						// Log.printVerbose("Found Receipt " + srcId +
						Log.printDebug("Found Receipt " + srcId + " for " + payNo);
					} else
					{
						Log.printDebug("Could not find Receipt " + payNo);
						continue;
					}
					findRecStmt.close();
					// srcTable = ReceiptBean.TABLENAME;
					srcTable = OfficialReceiptBean.TABLENAME;
					// negate the payamt
					payAmt = payAmt.negate();
					lPayAmt = lPayAmt.negate();
				} // end payType = P
				// SOURCE = CreditNote (C)
				else if (payType.equals("C"))
				{
					findCNStmt.setString(1, findPayNo);
					ResultSet rsFindCN = findCNStmt.executeQuery();
					if (rsFindCN.getFetchSize() > 1)
					{
						// This shouldn't happen, shows that the query is not
						// unique
						Log.printDebug("*** CN: MORE THAN ONE ROW FOUND!! ***");
					}
					if (rsFindCN.next())
					{
						srcId = new Long(rsFindCN.getLong("pkid"));
						// Log.printVerbose("Found CN " + srcId +
						Log.printDebug("Found CN " + srcId + " for " + payNo);
					} else
					{
						Log.printDebug("Could not find CN " + payNo);
						continue;
					}
					findCNStmt.close();
					srcTable = GenericStmtBean.TABLENAME;
					// negate the payamt
					payAmt = payAmt.negate();
					lPayAmt = lPayAmt.negate();
				} // end payType = C
				// TARGET = Invoice(I)
				if (payRefTxType.equals("I"))
				{
					findInvStmt.setString(1, findTxRefNo);
					ResultSet rsFindInv = findInvStmt.executeQuery();
					if (rsFindInv.getFetchSize() > 1)
					{
						// This shouldn't happen, shows that the query is not
						// unique
						Log.printDebug("*** INV: MORE THAN ONE ROW FOUND!! ***");
					}
					if (rsFindInv.next())
					{
						tgtId = new Long(rsFindInv.getLong("pkid"));
						// Log.printVerbose("Found Invoice " + tgtId +
						Log.printDebug("Found Invoice " + tgtId + " for " + payRefDTxRefNo);
					} else
					{
						Log.printDebug("Could not find Invoice " + payRefDTxRefNo);
						// tgtId = new Long(-1);
						continue;
					}
					findInvStmt.close();
					tgtTable = InvoiceBean.TABLENAME;
					// We should decrement the Invoice Amount here
					Invoice thisInv = InvoiceNut.getHandle(tgtId);
					thisInv.setOutstandingAmt(thisInv.getOutstandingAmt().subtract(lPayAmt.abs()));
					if (payType.equals("P"))
						relType = DocLinkBean.RELTYPE_PYMT_INV;
					else if (payType.equals("C"))
						relType = DocLinkBean.RELTYPE_CN_INV;
				} // end txType = I
				// TARGET = DebitNote(D)
				else if (payRefTxType.equals("D"))
				{
					findDNStmt.setString(1, findTxRefNo);
					ResultSet rsFindDN = findDNStmt.executeQuery();
					if (rsFindDN.getFetchSize() > 1)
					{
						// This shouldn't happen, shows that the query is not
						// unique
						Log.printDebug("*** DN: MORE THAN ONE ROW FOUND!! ***");
					}
					if (rsFindDN.next())
					{
						tgtId = new Long(rsFindDN.getLong("pkid"));
						// Log.printVerbose("Found DN " + tgtId +
						Log.printDebug("Found DN " + tgtId + " for " + payRefDTxRefNo);
					} else
					{
						Log.printDebug("Could not find DN " + payRefDTxRefNo);
						// tgtId = new Long(-1);
						continue;
					}
					findDNStmt.close();
					tgtTable = GenericStmtBean.TABLENAME;
					if (payType.equals("P"))
						relType = DocLinkBean.RELTYPE_PYMT_DN;
					else if (payType.equals("C"))
						relType = DocLinkBean.RELTYPE_CN_DN;
				} // end txType = D
				// TARGET = ReversePayment(R)
				else if (payRefTxType.equals("R"))
				{
					findRPayStmt.setString(1, findTxRefNo);
					ResultSet rsFindRPay = findRPayStmt.executeQuery();
					if (rsFindRPay.getFetchSize() > 1)
					{
						// This shouldn't happen, shows that the query is not
						// unique
						Log.printDebug("*** ReversePayment: MORE THAN ONE ROW FOUND!! ***");
					}
					if (rsFindRPay.next())
					{
						tgtId = new Long(rsFindRPay.getLong("pkid"));
						// Log.printVerbose("Found ReversePayment " + tgtId +
						Log.printDebug("Found ReversePayment " + tgtId + " for " + payRefDTxRefNo);
					} else
					{
						Log.printDebug("Could not find ReversePayment " + payRefDTxRefNo);
						// tgtId = new Long(-1);
						continue;
					}
					findRPayStmt.close();
					tgtTable = GenericStmtBean.TABLENAME;
					if (payType.equals("P"))
						relType = DocLinkBean.RELTYPE_REV_PYMT;
					else if (payType.equals("C"))
						relType = DocLinkBean.RELTYPE_REV_CN;
				} // end txType = R
				// Create the DocLink (if it does not already exist)
				// Filter for dups
				ifDupStmt.setString(1, srcTable);
				ifDupStmt.setLong(2, srcId.longValue());
				ifDupStmt.setString(3, tgtTable);
				ifDupStmt.setLong(4, tgtId.longValue());
				ResultSet rsIsDup = ifDupStmt.executeQuery();
				if (rsIsDup.getFetchSize() > 0)
				{
					Log.printDebug("*** DUPLICATE DETECTED for " + "(" + srcTable + ", " + srcId.toString() + ") and "
							+ "(" + tgtTable + ", " + tgtId.toString() + ")");
				} else
				{
					DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", relType, srcTable,
							srcId, tgtTable, tgtId, strDefCurr, lPayAmt, "", payAmt, "", TimeFormat.getTimestamp(),
							usrid);
				}
			} // while
			doc2DocStmt.close();
		} // end huge try
		catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while linking documents: " + ex.getMessage());
		}
		Log.printVerbose("***** END: DOCLINK *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "DOCLINK");
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
import java.math.BigDecimal;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;

public class DoDocLink implements Action
{
	private String strClassName = "DoDocLink";
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
		Log.printVerbose("***** BEGIN: DOCLINK *****");
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
			 * // 4. Perform Document Linking
			 ******************************************************************/
			// Clear the doclink table
			String clearDocLink = "delete from acc_doclink";
			Statement jbossStmt = jbossCon.createStatement();
			jbossStmt.executeUpdate(clearDocLink);
			String resetInvOutstandingBal = "update cust_invoice_index set outstanding_amt = total_amt";
			jbossStmt.executeUpdate(resetInvOutstandingBal);
			// Integer offset = new Integer(1059);
			// Integer limit = new Integer(1000);
			Integer offset = null;
			Integer limit = null;
			String doc2DocQ = "select * from payrefd";
			if (offset != null)
				doc2DocQ += " offset " + offset.intValue();
			if (limit != null)
				doc2DocQ += " limit " + limit.intValue();
			Statement doc2DocStmt = con.createStatement();
			ResultSet rsDoc2Doc = doc2DocStmt.executeQuery(doc2DocQ);
			/*
			 * // There are 6 distinct relationships between docs: topcon=#
			 * select distinct paytype,txtype from payrefd; paytype | txtype
			 * ---------+-------- C | D C | I C | R P | D P | I P | R
			 * 
			 * where
			 * C=CreditNote,D=DebitNote,I=Invoice,P=Payment,R=ReversePayment
			 */
			String ifDupQ = "select pkid from acc_doclink where " + "src_docref = ? and src_docid = ? and "
					+ "tgt_docref = ? and tgt_docid = ?";
			PreparedStatement ifDupStmt = jbossCon.prepareStatement(ifDupQ);
			String findInvQ = "select pkid from cust_invoice_index where remarks LIKE ?";
			PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
			String findCNQ = "select pkid from acc_generic_stmt where remarks LIKE ?" + " and stmt_type = '"
					+ GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE + "'";
			PreparedStatement findCNStmt = jbossCon.prepareStatement(findCNQ);
			String findDNQ = "select pkid from acc_generic_stmt where remarks LIKE ?" + " and stmt_type = '"
					+ GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE + "'";
			PreparedStatement findDNStmt = jbossCon.prepareStatement(findDNQ);
			/*
			 * String findRecQ = "select pkid from cust_receipt_index " + "where
			 * payment_remarks ~ ? and cust_account = ?"; PreparedStatement
			 * findRecStmt = jbossCon.prepareStatement(findRecQ);
			 */
			String findRecQ = "select pkid from acc_receipt_index " + "where payment_remarks LIKE ? and entity_key = ?";
			PreparedStatement findRecStmt = jbossCon.prepareStatement(findRecQ);
			String findRPayQ = "select pkid from acc_generic_stmt where remarks LIKE ?" + " and stmt_type = '"
					+ GenericStmtBean.STMT_TYPE_REVERSE_PAYMENT + "'";
			PreparedStatement findRPayStmt = jbossCon.prepareStatement(findRPayQ);
			curTask = new Task("Perform DocLink", rsDoc2Doc.getFetchSize());
			int count = (offset == null) ? 0 : offset.intValue();
			while (rsDoc2Doc.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				// Log.printVerbose("Processing Txn " + ++count);
				Log.printDebug("Processing Txn " + ++count);
				String payNo = rsDoc2Doc.getString("payno");
				String payType = rsDoc2Doc.getString("paytype");
				String payInfo = rsDoc2Doc.getString("payinfo");
				BigDecimal payRefDExchRate = rsDoc2Doc.getBigDecimal("exch_rate");
				String payRefDTxRefNo = rsDoc2Doc.getString("txrefno");
				String payRefDTxInfo = rsDoc2Doc.getString("txinfo");
				String payRefTxType = rsDoc2Doc.getString("txtype");
				BigDecimal payRefDTxExchRate = rsDoc2Doc.getBigDecimal("txexch_rate");
				Timestamp payRefDTxDate = TimeFormat
						.createTimeStamp(rsDoc2Doc.getString("txdate"), "MM/dd/yy HH:mm:ss");
				Timestamp payDate = TimeFormat.createTimeStamp(rsDoc2Doc.getString("paydate"), "MM/dd/yy HH:mm:ss");
				BigDecimal payAmt = rsDoc2Doc.getBigDecimal("payamt");
				BigDecimal lPayAmt = rsDoc2Doc.getBigDecimal("lpayamt");
				BigDecimal payRefDLTxAmt = rsDoc2Doc.getBigDecimal("ltxamt");
				String posted = rsDoc2Doc.getString("posted");
				String mthEnd = rsDoc2Doc.getString("mthend");
				String payRefDCustId = rsDoc2Doc.getString("custId");
				String srcTable = null;
				String tgtTable = null;
				Long srcId = null;
				Long tgtId = null;
				String relType = null;
				String findPayNo = "% = " + payNo + ")%";
				String findTxRefNo = "% = " + payRefDTxRefNo + ")%";
				// SOURCE = Payment (P)
				if (payType.equals("P"))
				{
					// Find the custAccId given custCode
					CustAccount thisCustAcc = CustAccountNut.getObjectByCode(payRefDCustId);
					if (thisCustAcc == null)
					{
						Log.printDebug(payRefDCustId + ": Customer Code not found!!");
						continue;
					}
					findRecStmt.setString(1, findPayNo);
					findRecStmt.setInt(2, thisCustAcc.getPkid().intValue());
					ResultSet rsFindRec = findRecStmt.executeQuery();
					if (rsFindRec.getFetchSize() > 2) // Could have duplicates
					{
						// This shouldn't happen, shows that the query is not
						// unique
						Log.printDebug("*** Receipt: MORE THAN TWO ROWS FOUND!! ***");
					}
					if (rsFindRec.next())
					{
						srcId = new Long(rsFindRec.getLong("pkid"));
						// Log.printVerbose("Found Receipt " + srcId +
						Log.printDebug("Found Receipt " + srcId + " for " + payNo);
					} else
					{
						Log.printDebug("Could not find Receipt " + payNo);
						continue;
					}
					findRecStmt.close();
					// srcTable = ReceiptBean.TABLENAME;
					srcTable = OfficialReceiptBean.TABLENAME;
					// negate the payamt
					payAmt = payAmt.negate();
					lPayAmt = lPayAmt.negate();
				} // end payType = P
				// SOURCE = CreditNote (C)
				else if (payType.equals("C"))
				{
					findCNStmt.setString(1, findPayNo);
					ResultSet rsFindCN = findCNStmt.executeQuery();
					if (rsFindCN.getFetchSize() > 1)
					{
						// This shouldn't happen, shows that the query is not
						// unique
						Log.printDebug("*** CN: MORE THAN ONE ROW FOUND!! ***");
					}
					if (rsFindCN.next())
					{
						srcId = new Long(rsFindCN.getLong("pkid"));
						// Log.printVerbose("Found CN " + srcId +
						Log.printDebug("Found CN " + srcId + " for " + payNo);
					} else
					{
						Log.printDebug("Could not find CN " + payNo);
						continue;
					}
					findCNStmt.close();
					srcTable = GenericStmtBean.TABLENAME;
					// negate the payamt
					payAmt = payAmt.negate();
					lPayAmt = lPayAmt.negate();
				} // end payType = C
				// TARGET = Invoice(I)
				if (payRefTxType.equals("I"))
				{
					findInvStmt.setString(1, findTxRefNo);
					ResultSet rsFindInv = findInvStmt.executeQuery();
					if (rsFindInv.getFetchSize() > 1)
					{
						// This shouldn't happen, shows that the query is not
						// unique
						Log.printDebug("*** INV: MORE THAN ONE ROW FOUND!! ***");
					}
					if (rsFindInv.next())
					{
						tgtId = new Long(rsFindInv.getLong("pkid"));
						// Log.printVerbose("Found Invoice " + tgtId +
						Log.printDebug("Found Invoice " + tgtId + " for " + payRefDTxRefNo);
					} else
					{
						Log.printDebug("Could not find Invoice " + payRefDTxRefNo);
						// tgtId = new Long(-1);
						continue;
					}
					findInvStmt.close();
					tgtTable = InvoiceBean.TABLENAME;
					// We should decrement the Invoice Amount here
					Invoice thisInv = InvoiceNut.getHandle(tgtId);
					thisInv.setOutstandingAmt(thisInv.getOutstandingAmt().subtract(lPayAmt.abs()));
					if (payType.equals("P"))
						relType = DocLinkBean.RELTYPE_PYMT_INV;
					else if (payType.equals("C"))
						relType = DocLinkBean.RELTYPE_CN_INV;
				} // end txType = I
				// TARGET = DebitNote(D)
				else if (payRefTxType.equals("D"))
				{
					findDNStmt.setString(1, findTxRefNo);
					ResultSet rsFindDN = findDNStmt.executeQuery();
					if (rsFindDN.getFetchSize() > 1)
					{
						// This shouldn't happen, shows that the query is not
						// unique
						Log.printDebug("*** DN: MORE THAN ONE ROW FOUND!! ***");
					}
					if (rsFindDN.next())
					{
						tgtId = new Long(rsFindDN.getLong("pkid"));
						// Log.printVerbose("Found DN " + tgtId +
						Log.printDebug("Found DN " + tgtId + " for " + payRefDTxRefNo);
					} else
					{
						Log.printDebug("Could not find DN " + payRefDTxRefNo);
						// tgtId = new Long(-1);
						continue;
					}
					findDNStmt.close();
					tgtTable = GenericStmtBean.TABLENAME;
					if (payType.equals("P"))
						relType = DocLinkBean.RELTYPE_PYMT_DN;
					else if (payType.equals("C"))
						relType = DocLinkBean.RELTYPE_CN_DN;
				} // end txType = D
				// TARGET = ReversePayment(R)
				else if (payRefTxType.equals("R"))
				{
					findRPayStmt.setString(1, findTxRefNo);
					ResultSet rsFindRPay = findRPayStmt.executeQuery();
					if (rsFindRPay.getFetchSize() > 1)
					{
						// This shouldn't happen, shows that the query is not
						// unique
						Log.printDebug("*** ReversePayment: MORE THAN ONE ROW FOUND!! ***");
					}
					if (rsFindRPay.next())
					{
						tgtId = new Long(rsFindRPay.getLong("pkid"));
						// Log.printVerbose("Found ReversePayment " + tgtId +
						Log.printDebug("Found ReversePayment " + tgtId + " for " + payRefDTxRefNo);
					} else
					{
						Log.printDebug("Could not find ReversePayment " + payRefDTxRefNo);
						// tgtId = new Long(-1);
						continue;
					}
					findRPayStmt.close();
					tgtTable = GenericStmtBean.TABLENAME;
					if (payType.equals("P"))
						relType = DocLinkBean.RELTYPE_REV_PYMT;
					else if (payType.equals("C"))
						relType = DocLinkBean.RELTYPE_REV_CN;
				} // end txType = R
				// Create the DocLink (if it does not already exist)
				// Filter for dups
				ifDupStmt.setString(1, srcTable);
				ifDupStmt.setLong(2, srcId.longValue());
				ifDupStmt.setString(3, tgtTable);
				ifDupStmt.setLong(4, tgtId.longValue());
				ResultSet rsIsDup = ifDupStmt.executeQuery();
				if (rsIsDup.getFetchSize() > 0)
				{
					Log.printDebug("*** DUPLICATE DETECTED for " + "(" + srcTable + ", " + srcId.toString() + ") and "
							+ "(" + tgtTable + ", " + tgtId.toString() + ")");
				} else
				{
					DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", relType, srcTable,
							srcId, tgtTable, tgtId, strDefCurr, lPayAmt, "", payAmt, "", TimeFormat.getTimestamp(),
							usrid);
				}
			} // while
			doc2DocStmt.close();
		} // end huge try
		catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while linking documents: " + ex.getMessage());
		}
		Log.printVerbose("***** END: DOCLINK *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "DOCLINK");
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
import java.math.BigDecimal;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;

public class DoDocLink implements Action
{
	private String strClassName = "DoDocLink";
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
		Log.printVerbose("***** BEGIN: DOCLINK *****");
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
			 * // 4. Perform Document Linking
			 ******************************************************************/
			// Clear the doclink table
			String clearDocLink = "delete from acc_doclink";
			Statement jbossStmt = jbossCon.createStatement();
			jbossStmt.executeUpdate(clearDocLink);
			String resetInvOutstandingBal = "update cust_invoice_index set outstanding_amt = total_amt";
			jbossStmt.executeUpdate(resetInvOutstandingBal);
			// Integer offset = new Integer(1059);
			// Integer limit = new Integer(1000);
			Integer offset = null;
			Integer limit = null;
			String doc2DocQ = "select * from payrefd";
			if (offset != null)
				doc2DocQ += " offset " + offset.intValue();
			if (limit != null)
				doc2DocQ += " limit " + limit.intValue();
			Statement doc2DocStmt = con.createStatement();
			ResultSet rsDoc2Doc = doc2DocStmt.executeQuery(doc2DocQ);
			/*
			 * // There are 6 distinct relationships between docs: topcon=#
			 * select distinct paytype,txtype from payrefd; paytype | txtype
			 * ---------+-------- C | D C | I C | R P | D P | I P | R
			 * 
			 * where
			 * C=CreditNote,D=DebitNote,I=Invoice,P=Payment,R=ReversePayment
			 */
			String ifDupQ = "select pkid from acc_doclink where " + "src_docref = ? and src_docid = ? and "
					+ "tgt_docref = ? and tgt_docid = ?";
			PreparedStatement ifDupStmt = jbossCon.prepareStatement(ifDupQ);
			String findInvQ = "select pkid from cust_invoice_index where remarks LIKE ?";
			PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
			String findCNQ = "select pkid from acc_generic_stmt where remarks LIKE ?" + " and stmt_type = '"
					+ GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE + "'";
			PreparedStatement findCNStmt = jbossCon.prepareStatement(findCNQ);
			String findDNQ = "select pkid from acc_generic_stmt where remarks LIKE ?" + " and stmt_type = '"
					+ GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE + "'";
			PreparedStatement findDNStmt = jbossCon.prepareStatement(findDNQ);
			/*
			 * String findRecQ = "select pkid from cust_receipt_index " + "where
			 * payment_remarks ~ ? and cust_account = ?"; PreparedStatement
			 * findRecStmt = jbossCon.prepareStatement(findRecQ);
			 */
			String findRecQ = "select pkid from acc_receipt_index " + "where payment_remarks LIKE ? and entity_key = ?";
			PreparedStatement findRecStmt = jbossCon.prepareStatement(findRecQ);
			String findRPayQ = "select pkid from acc_generic_stmt where remarks LIKE ?" + " and stmt_type = '"
					+ GenericStmtBean.STMT_TYPE_REVERSE_PAYMENT + "'";
			PreparedStatement findRPayStmt = jbossCon.prepareStatement(findRPayQ);
			curTask = new Task("Perform DocLink", rsDoc2Doc.getFetchSize());
			int count = (offset == null) ? 0 : offset.intValue();
			while (rsDoc2Doc.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				// Log.printVerbose("Processing Txn " + ++count);
				Log.printDebug("Processing Txn " + ++count);
				String payNo = rsDoc2Doc.getString("payno");
				String payType = rsDoc2Doc.getString("paytype");
				String payInfo = rsDoc2Doc.getString("payinfo");
				BigDecimal payRefDExchRate = rsDoc2Doc.getBigDecimal("exch_rate");
				String payRefDTxRefNo = rsDoc2Doc.getString("txrefno");
				String payRefDTxInfo = rsDoc2Doc.getString("txinfo");
				String payRefTxType = rsDoc2Doc.getString("txtype");
				BigDecimal payRefDTxExchRate = rsDoc2Doc.getBigDecimal("txexch_rate");
				Timestamp payRefDTxDate = TimeFormat
						.createTimeStamp(rsDoc2Doc.getString("txdate"), "MM/dd/yy HH:mm:ss");
				Timestamp payDate = TimeFormat.createTimeStamp(rsDoc2Doc.getString("paydate"), "MM/dd/yy HH:mm:ss");
				BigDecimal payAmt = rsDoc2Doc.getBigDecimal("payamt");
				BigDecimal lPayAmt = rsDoc2Doc.getBigDecimal("lpayamt");
				BigDecimal payRefDLTxAmt = rsDoc2Doc.getBigDecimal("ltxamt");
				String posted = rsDoc2Doc.getString("posted");
				String mthEnd = rsDoc2Doc.getString("mthend");
				String payRefDCustId = rsDoc2Doc.getString("custId");
				String srcTable = null;
				String tgtTable = null;
				Long srcId = null;
				Long tgtId = null;
				String relType = null;
				String findPayNo = "% = " + payNo + ")%";
				String findTxRefNo = "% = " + payRefDTxRefNo + ")%";
				// SOURCE = Payment (P)
				if (payType.equals("P"))
				{
					// Find the custAccId given custCode
					CustAccount thisCustAcc = CustAccountNut.getObjectByCode(payRefDCustId);
					if (thisCustAcc == null)
					{
						Log.printDebug(payRefDCustId + ": Customer Code not found!!");
						continue;
					}
					findRecStmt.setString(1, findPayNo);
					findRecStmt.setInt(2, thisCustAcc.getPkid().intValue());
					ResultSet rsFindRec = findRecStmt.executeQuery();
					if (rsFindRec.getFetchSize() > 2) // Could have duplicates
					{
						// This shouldn't happen, shows that the query is not
						// unique
						Log.printDebug("*** Receipt: MORE THAN TWO ROWS FOUND!! ***");
					}
					if (rsFindRec.next())
					{
						srcId = new Long(rsFindRec.getLong("pkid"));
						// Log.printVerbose("Found Receipt " + srcId +
						Log.printDebug("Found Receipt " + srcId + " for " + payNo);
					} else
					{
						Log.printDebug("Could not find Receipt " + payNo);
						continue;
					}
					findRecStmt.close();
					// srcTable = ReceiptBean.TABLENAME;
					srcTable = OfficialReceiptBean.TABLENAME;
					// negate the payamt
					payAmt = payAmt.negate();
					lPayAmt = lPayAmt.negate();
				} // end payType = P
				// SOURCE = CreditNote (C)
				else if (payType.equals("C"))
				{
					findCNStmt.setString(1, findPayNo);
					ResultSet rsFindCN = findCNStmt.executeQuery();
					if (rsFindCN.getFetchSize() > 1)
					{
						// This shouldn't happen, shows that the query is not
						// unique
						Log.printDebug("*** CN: MORE THAN ONE ROW FOUND!! ***");
					}
					if (rsFindCN.next())
					{
						srcId = new Long(rsFindCN.getLong("pkid"));
						// Log.printVerbose("Found CN " + srcId +
						Log.printDebug("Found CN " + srcId + " for " + payNo);
					} else
					{
						Log.printDebug("Could not find CN " + payNo);
						continue;
					}
					findCNStmt.close();
					srcTable = GenericStmtBean.TABLENAME;
					// negate the payamt
					payAmt = payAmt.negate();
					lPayAmt = lPayAmt.negate();
				} // end payType = C
				// TARGET = Invoice(I)
				if (payRefTxType.equals("I"))
				{
					findInvStmt.setString(1, findTxRefNo);
					ResultSet rsFindInv = findInvStmt.executeQuery();
					if (rsFindInv.getFetchSize() > 1)
					{
						// This shouldn't happen, shows that the query is not
						// unique
						Log.printDebug("*** INV: MORE THAN ONE ROW FOUND!! ***");
					}
					if (rsFindInv.next())
					{
						tgtId = new Long(rsFindInv.getLong("pkid"));
						// Log.printVerbose("Found Invoice " + tgtId +
						Log.printDebug("Found Invoice " + tgtId + " for " + payRefDTxRefNo);
					} else
					{
						Log.printDebug("Could not find Invoice " + payRefDTxRefNo);
						// tgtId = new Long(-1);
						continue;
					}
					findInvStmt.close();
					tgtTable = InvoiceBean.TABLENAME;
					// We should decrement the Invoice Amount here
					Invoice thisInv = InvoiceNut.getHandle(tgtId);
					thisInv.setOutstandingAmt(thisInv.getOutstandingAmt().subtract(lPayAmt.abs()));
					if (payType.equals("P"))
						relType = DocLinkBean.RELTYPE_PYMT_INV;
					else if (payType.equals("C"))
						relType = DocLinkBean.RELTYPE_CN_INV;
				} // end txType = I
				// TARGET = DebitNote(D)
				else if (payRefTxType.equals("D"))
				{
					findDNStmt.setString(1, findTxRefNo);
					ResultSet rsFindDN = findDNStmt.executeQuery();
					if (rsFindDN.getFetchSize() > 1)
					{
						// This shouldn't happen, shows that the query is not
						// unique
						Log.printDebug("*** DN: MORE THAN ONE ROW FOUND!! ***");
					}
					if (rsFindDN.next())
					{
						tgtId = new Long(rsFindDN.getLong("pkid"));
						// Log.printVerbose("Found DN " + tgtId +
						Log.printDebug("Found DN " + tgtId + " for " + payRefDTxRefNo);
					} else
					{
						Log.printDebug("Could not find DN " + payRefDTxRefNo);
						// tgtId = new Long(-1);
						continue;
					}
					findDNStmt.close();
					tgtTable = GenericStmtBean.TABLENAME;
					if (payType.equals("P"))
						relType = DocLinkBean.RELTYPE_PYMT_DN;
					else if (payType.equals("C"))
						relType = DocLinkBean.RELTYPE_CN_DN;
				} // end txType = D
				// TARGET = ReversePayment(R)
				else if (payRefTxType.equals("R"))
				{
					findRPayStmt.setString(1, findTxRefNo);
					ResultSet rsFindRPay = findRPayStmt.executeQuery();
					if (rsFindRPay.getFetchSize() > 1)
					{
						// This shouldn't happen, shows that the query is not
						// unique
						Log.printDebug("*** ReversePayment: MORE THAN ONE ROW FOUND!! ***");
					}
					if (rsFindRPay.next())
					{
						tgtId = new Long(rsFindRPay.getLong("pkid"));
						// Log.printVerbose("Found ReversePayment " + tgtId +
						Log.printDebug("Found ReversePayment " + tgtId + " for " + payRefDTxRefNo);
					} else
					{
						Log.printDebug("Could not find ReversePayment " + payRefDTxRefNo);
						// tgtId = new Long(-1);
						continue;
					}
					findRPayStmt.close();
					tgtTable = GenericStmtBean.TABLENAME;
					if (payType.equals("P"))
						relType = DocLinkBean.RELTYPE_REV_PYMT;
					else if (payType.equals("C"))
						relType = DocLinkBean.RELTYPE_REV_CN;
				} // end txType = R
				// Create the DocLink (if it does not already exist)
				// Filter for dups
				ifDupStmt.setString(1, srcTable);
				ifDupStmt.setLong(2, srcId.longValue());
				ifDupStmt.setString(3, tgtTable);
				ifDupStmt.setLong(4, tgtId.longValue());
				ResultSet rsIsDup = ifDupStmt.executeQuery();
				if (rsIsDup.getFetchSize() > 0)
				{
					Log.printDebug("*** DUPLICATE DETECTED for " + "(" + srcTable + ", " + srcId.toString() + ") and "
							+ "(" + tgtTable + ", " + tgtId.toString() + ")");
				} else
				{
					DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", relType, srcTable,
							srcId, tgtTable, tgtId, strDefCurr, lPayAmt, "", payAmt, "", TimeFormat.getTimestamp(),
							usrid);
				}
			} // while
			doc2DocStmt.close();
		} // end huge try
		catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while linking documents: " + ex.getMessage());
		}
		Log.printVerbose("***** END: DOCLINK *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "DOCLINK");
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
import java.math.BigDecimal;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;

public class DoDocLink implements Action
{
	private String strClassName = "DoDocLink";
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
		Log.printVerbose("***** BEGIN: DOCLINK *****");
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
			 * // 4. Perform Document Linking
			 ******************************************************************/
			// Clear the doclink table
			String clearDocLink = "delete from acc_doclink";
			Statement jbossStmt = jbossCon.createStatement();
			jbossStmt.executeUpdate(clearDocLink);
			String resetInvOutstandingBal = "update cust_invoice_index set outstanding_amt = total_amt";
			jbossStmt.executeUpdate(resetInvOutstandingBal);
			// Integer offset = new Integer(1059);
			// Integer limit = new Integer(1000);
			Integer offset = null;
			Integer limit = null;
			String doc2DocQ = "select * from payrefd";
			if (offset != null)
				doc2DocQ += " offset " + offset.intValue();
			if (limit != null)
				doc2DocQ += " limit " + limit.intValue();
			Statement doc2DocStmt = con.createStatement();
			ResultSet rsDoc2Doc = doc2DocStmt.executeQuery(doc2DocQ);
			/*
			 * // There are 6 distinct relationships between docs: topcon=#
			 * select distinct paytype,txtype from payrefd; paytype | txtype
			 * ---------+-------- C | D C | I C | R P | D P | I P | R
			 * 
			 * where
			 * C=CreditNote,D=DebitNote,I=Invoice,P=Payment,R=ReversePayment
			 */
			String ifDupQ = "select pkid from acc_doclink where " + "src_docref = ? and src_docid = ? and "
					+ "tgt_docref = ? and tgt_docid = ?";
			PreparedStatement ifDupStmt = jbossCon.prepareStatement(ifDupQ);
			String findInvQ = "select pkid from cust_invoice_index where remarks LIKE ?";
			PreparedStatement findInvStmt = jbossCon.prepareStatement(findInvQ);
			String findCNQ = "select pkid from acc_generic_stmt where remarks LIKE ?" + " and stmt_type = '"
					+ GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE + "'";
			PreparedStatement findCNStmt = jbossCon.prepareStatement(findCNQ);
			String findDNQ = "select pkid from acc_generic_stmt where remarks LIKE ?" + " and stmt_type = '"
					+ GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE + "'";
			PreparedStatement findDNStmt = jbossCon.prepareStatement(findDNQ);
			/*
			 * String findRecQ = "select pkid from cust_receipt_index " + "where
			 * payment_remarks ~ ? and cust_account = ?"; PreparedStatement
			 * findRecStmt = jbossCon.prepareStatement(findRecQ);
			 */
			String findRecQ = "select pkid from acc_receipt_index " + "where payment_remarks LIKE ? and entity_key = ?";
			PreparedStatement findRecStmt = jbossCon.prepareStatement(findRecQ);
			String findRPayQ = "select pkid from acc_generic_stmt where remarks LIKE ?" + " and stmt_type = '"
					+ GenericStmtBean.STMT_TYPE_REVERSE_PAYMENT + "'";
			PreparedStatement findRPayStmt = jbossCon.prepareStatement(findRPayQ);
			curTask = new Task("Perform DocLink", rsDoc2Doc.getFetchSize());
			int count = (offset == null) ? 0 : offset.intValue();
			while (rsDoc2Doc.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				// Log.printVerbose("Processing Txn " + ++count);
				Log.printDebug("Processing Txn " + ++count);
				String payNo = rsDoc2Doc.getString("payno");
				String payType = rsDoc2Doc.getString("paytype");
				String payInfo = rsDoc2Doc.getString("payinfo");
				BigDecimal payRefDExchRate = rsDoc2Doc.getBigDecimal("exch_rate");
				String payRefDTxRefNo = rsDoc2Doc.getString("txrefno");
				String payRefDTxInfo = rsDoc2Doc.getString("txinfo");
				String payRefTxType = rsDoc2Doc.getString("txtype");
				BigDecimal payRefDTxExchRate = rsDoc2Doc.getBigDecimal("txexch_rate");
				Timestamp payRefDTxDate = TimeFormat
						.createTimeStamp(rsDoc2Doc.getString("txdate"), "MM/dd/yy HH:mm:ss");
				Timestamp payDate = TimeFormat.createTimeStamp(rsDoc2Doc.getString("paydate"), "MM/dd/yy HH:mm:ss");
				BigDecimal payAmt = rsDoc2Doc.getBigDecimal("payamt");
				BigDecimal lPayAmt = rsDoc2Doc.getBigDecimal("lpayamt");
				BigDecimal payRefDLTxAmt = rsDoc2Doc.getBigDecimal("ltxamt");
				String posted = rsDoc2Doc.getString("posted");
				String mthEnd = rsDoc2Doc.getString("mthend");
				String payRefDCustId = rsDoc2Doc.getString("custId");
				String srcTable = null;
				String tgtTable = null;
				Long srcId = null;
				Long tgtId = null;
				String relType = null;
				String findPayNo = "% = " + payNo + ")%";
				String findTxRefNo = "% = " + payRefDTxRefNo + ")%";
				// SOURCE = Payment (P)
				if (payType.equals("P"))
				{
					// Find the custAccId given custCode
					CustAccount thisCustAcc = CustAccountNut.getObjectByCode(payRefDCustId);
					if (thisCustAcc == null)
					{
						Log.printDebug(payRefDCustId + ": Customer Code not found!!");
						continue;
					}
					findRecStmt.setString(1, findPayNo);
					findRecStmt.setInt(2, thisCustAcc.getPkid().intValue());
					ResultSet rsFindRec = findRecStmt.executeQuery();
					if (rsFindRec.getFetchSize() > 2) // Could have duplicates
					{
						// This shouldn't happen, shows that the query is not
						// unique
						Log.printDebug("*** Receipt: MORE THAN TWO ROWS FOUND!! ***");
					}
					if (rsFindRec.next())
					{
						srcId = new Long(rsFindRec.getLong("pkid"));
						// Log.printVerbose("Found Receipt " + srcId +
						Log.printDebug("Found Receipt " + srcId + " for " + payNo);
					} else
					{
						Log.printDebug("Could not find Receipt " + payNo);
						continue;
					}
					findRecStmt.close();
					// srcTable = ReceiptBean.TABLENAME;
					srcTable = OfficialReceiptBean.TABLENAME;
					// negate the payamt
					payAmt = payAmt.negate();
					lPayAmt = lPayAmt.negate();
				} // end payType = P
				// SOURCE = CreditNote (C)
				else if (payType.equals("C"))
				{
					findCNStmt.setString(1, findPayNo);
					ResultSet rsFindCN = findCNStmt.executeQuery();
					if (rsFindCN.getFetchSize() > 1)
					{
						// This shouldn't happen, shows that the query is not
						// unique
						Log.printDebug("*** CN: MORE THAN ONE ROW FOUND!! ***");
					}
					if (rsFindCN.next())
					{
						srcId = new Long(rsFindCN.getLong("pkid"));
						// Log.printVerbose("Found CN " + srcId +
						Log.printDebug("Found CN " + srcId + " for " + payNo);
					} else
					{
						Log.printDebug("Could not find CN " + payNo);
						continue;
					}
					findCNStmt.close();
					srcTable = GenericStmtBean.TABLENAME;
					// negate the payamt
					payAmt = payAmt.negate();
					lPayAmt = lPayAmt.negate();
				} // end payType = C
				// TARGET = Invoice(I)
				if (payRefTxType.equals("I"))
				{
					findInvStmt.setString(1, findTxRefNo);
					ResultSet rsFindInv = findInvStmt.executeQuery();
					if (rsFindInv.getFetchSize() > 1)
					{
						// This shouldn't happen, shows that the query is not
						// unique
						Log.printDebug("*** INV: MORE THAN ONE ROW FOUND!! ***");
					}
					if (rsFindInv.next())
					{
						tgtId = new Long(rsFindInv.getLong("pkid"));
						// Log.printVerbose("Found Invoice " + tgtId +
						Log.printDebug("Found Invoice " + tgtId + " for " + payRefDTxRefNo);
					} else
					{
						Log.printDebug("Could not find Invoice " + payRefDTxRefNo);
						// tgtId = new Long(-1);
						continue;
					}
					findInvStmt.close();
					tgtTable = InvoiceBean.TABLENAME;
					// We should decrement the Invoice Amount here
					Invoice thisInv = InvoiceNut.getHandle(tgtId);
					thisInv.setOutstandingAmt(thisInv.getOutstandingAmt().subtract(lPayAmt.abs()));
					if (payType.equals("P"))
						relType = DocLinkBean.RELTYPE_PYMT_INV;
					else if (payType.equals("C"))
						relType = DocLinkBean.RELTYPE_CN_INV;
				} // end txType = I
				// TARGET = DebitNote(D)
				else if (payRefTxType.equals("D"))
				{
					findDNStmt.setString(1, findTxRefNo);
					ResultSet rsFindDN = findDNStmt.executeQuery();
					if (rsFindDN.getFetchSize() > 1)
					{
						// This shouldn't happen, shows that the query is not
						// unique
						Log.printDebug("*** DN: MORE THAN ONE ROW FOUND!! ***");
					}
					if (rsFindDN.next())
					{
						tgtId = new Long(rsFindDN.getLong("pkid"));
						// Log.printVerbose("Found DN " + tgtId +
						Log.printDebug("Found DN " + tgtId + " for " + payRefDTxRefNo);
					} else
					{
						Log.printDebug("Could not find DN " + payRefDTxRefNo);
						// tgtId = new Long(-1);
						continue;
					}
					findDNStmt.close();
					tgtTable = GenericStmtBean.TABLENAME;
					if (payType.equals("P"))
						relType = DocLinkBean.RELTYPE_PYMT_DN;
					else if (payType.equals("C"))
						relType = DocLinkBean.RELTYPE_CN_DN;
				} // end txType = D
				// TARGET = ReversePayment(R)
				else if (payRefTxType.equals("R"))
				{
					findRPayStmt.setString(1, findTxRefNo);
					ResultSet rsFindRPay = findRPayStmt.executeQuery();
					if (rsFindRPay.getFetchSize() > 1)
					{
						// This shouldn't happen, shows that the query is not
						// unique
						Log.printDebug("*** ReversePayment: MORE THAN ONE ROW FOUND!! ***");
					}
					if (rsFindRPay.next())
					{
						tgtId = new Long(rsFindRPay.getLong("pkid"));
						// Log.printVerbose("Found ReversePayment " + tgtId +
						Log.printDebug("Found ReversePayment " + tgtId + " for " + payRefDTxRefNo);
					} else
					{
						Log.printDebug("Could not find ReversePayment " + payRefDTxRefNo);
						// tgtId = new Long(-1);
						continue;
					}
					findRPayStmt.close();
					tgtTable = GenericStmtBean.TABLENAME;
					if (payType.equals("P"))
						relType = DocLinkBean.RELTYPE_REV_PYMT;
					else if (payType.equals("C"))
						relType = DocLinkBean.RELTYPE_REV_CN;
				} // end txType = R
				// Create the DocLink (if it does not already exist)
				// Filter for dups
				ifDupStmt.setString(1, srcTable);
				ifDupStmt.setLong(2, srcId.longValue());
				ifDupStmt.setString(3, tgtTable);
				ifDupStmt.setLong(4, tgtId.longValue());
				ResultSet rsIsDup = ifDupStmt.executeQuery();
				if (rsIsDup.getFetchSize() > 0)
				{
					Log.printDebug("*** DUPLICATE DETECTED for " + "(" + srcTable + ", " + srcId.toString() + ") and "
							+ "(" + tgtTable + ", " + tgtId.toString() + ")");
				} else
				{
					DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", relType, srcTable,
							srcId, tgtTable, tgtId, strDefCurr, lPayAmt, "", payAmt, "", TimeFormat.getTimestamp(),
							usrid);
				}
			} // while
			doc2DocStmt.close();
		} // end huge try
		catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while linking documents: " + ex.getMessage());
		}
		Log.printVerbose("***** END: DOCLINK *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "DOCLINK");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		// Clean up curTask;
		curTask = null;
		return new ActionRouter("test-migrate-topcondb-page");
	}
}
