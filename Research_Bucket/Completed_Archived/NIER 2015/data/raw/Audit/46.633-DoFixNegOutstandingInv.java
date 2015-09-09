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

/**
 * 
 * DoFixNegOutstandingInv ====================== During Topcon's DB migration,
 * there had been some duplicate entries that has led to negative invoice
 * balances. To fix this, the following algorithm is proposed:
 * 
 * for each invoice with -ve outstanding bal, 1. select remarks from
 * cust_invoice_index where invoice_pkid=<outstanding_inv_pkid> and parse it to
 * obtain the Old Docno 2. select (ldebitamt-lcreditamt) from cdhis where docno=<Old
 * DocNo> = corrected_outstanding_amt 3. update cust_invoice_index set
 * outstanding_amt=<corrected_outstanding_amt>
 * 
 */
public class DoFixNegOutstandingInv implements Action
{
	private String strClassName = "DoFixNegOutstandingInv";
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
			return new ActionRouter("test-migrate-topcondb-page");
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
			// Select all invoices with -ve balance
			String sltInvWithNegBal = "select pkid, outstanding_amt, remarks from cust_invoice_index where outstanding_amt < 0";
			ResultSet rs = empStmt.executeQuery(sltInvWithNegBal);
			int count = 1;
			while (rs.next())
			{
				Log.printDebug("** Processing Entry: " + count++);
				Long lPkid = new Long(rs.getLong("pkid"));
				BigDecimal bdOutstandingAmt = rs.getBigDecimal("outstanding_amt");
				String sRemarks = rs.getString("remarks");
				// Parse the remarks to obtain the old docNo
				String oldDocNo = getOldDocNoFromRemarks(sRemarks);
				if (oldDocNo == null || oldDocNo.equals(""))
				{
					Log.printDebug("*** Failed to get DocNo from remarks: " + sRemarks);
					continue;
				}
				// select correct balance from topcon DB
				String sltBalFromCdHis = "select (ldebitamt-lcreditamt) as bal from cdhis where docno = '" + oldDocNo
						+ "'";
				ResultSet rsBal = topconStmt.executeQuery(sltBalFromCdHis);
				if (rsBal.next())
				{
					BigDecimal correctBal = rsBal.getBigDecimal("bal");
					// Update emp DB with correct bal
					Invoice inv = InvoiceNut.getHandle(lPkid);
					if (inv != null)
					{
						inv.setOutstandingAmt(correctBal);
						Log.printVerbose("Successfully set new outstanding_amt for Invoice " + lPkid + " to "
								+ correctBal);
					}
				}
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred in " + strClassName + ": " + ex.getMessage());
		}
		Log.printVerbose("***** END: FIX -VE OUTSTANDING INVOICE *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "FIX -VE OUTSTANDING INVOICE");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		// Clean up curTask;
		curTask = null;
		return new ActionRouter("test-migrate-topcondb-page");
	}

	public String getOldDocNoFromRemarks(String remarks)
	{
		String[] arrSplit = remarks.split(" = ");
		System.out.println(Arrays.asList(arrSplit));
		String ans = arrSplit[1].trim().substring(0, arrSplit[1].trim().indexOf(')'));
		System.out.println("Ans: " + ans);
		return ans;
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

/**
 * 
 * DoFixNegOutstandingInv ====================== During Topcon's DB migration,
 * there had been some duplicate entries that has led to negative invoice
 * balances. To fix this, the following algorithm is proposed:
 * 
 * for each invoice with -ve outstanding bal, 1. select remarks from
 * cust_invoice_index where invoice_pkid=<outstanding_inv_pkid> and parse it to
 * obtain the Old Docno 2. select (ldebitamt-lcreditamt) from cdhis where docno=<Old
 * DocNo> = corrected_outstanding_amt 3. update cust_invoice_index set
 * outstanding_amt=<corrected_outstanding_amt>
 * 
 */
public class DoFixNegOutstandingInv implements Action
{
	private String strClassName = "DoFixNegOutstandingInv";
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
			return new ActionRouter("test-migrate-topcondb-page");
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
			// Select all invoices with -ve balance
			String sltInvWithNegBal = "select pkid, outstanding_amt, remarks from cust_invoice_index where outstanding_amt < 0";
			ResultSet rs = empStmt.executeQuery(sltInvWithNegBal);
			int count = 1;
			while (rs.next())
			{
				Log.printDebug("** Processing Entry: " + count++);
				Long lPkid = new Long(rs.getLong("pkid"));
				BigDecimal bdOutstandingAmt = rs.getBigDecimal("outstanding_amt");
				String sRemarks = rs.getString("remarks");
				// Parse the remarks to obtain the old docNo
				String oldDocNo = getOldDocNoFromRemarks(sRemarks);
				if (oldDocNo == null || oldDocNo.equals(""))
				{
					Log.printDebug("*** Failed to get DocNo from remarks: " + sRemarks);
					continue;
				}
				// select correct balance from topcon DB
				String sltBalFromCdHis = "select (ldebitamt-lcreditamt) as bal from cdhis where docno = '" + oldDocNo
						+ "'";
				ResultSet rsBal = topconStmt.executeQuery(sltBalFromCdHis);
				if (rsBal.next())
				{
					BigDecimal correctBal = rsBal.getBigDecimal("bal");
					// Update emp DB with correct bal
					Invoice inv = InvoiceNut.getHandle(lPkid);
					if (inv != null)
					{
						inv.setOutstandingAmt(correctBal);
						Log.printVerbose("Successfully set new outstanding_amt for Invoice " + lPkid + " to "
								+ correctBal);
					}
				}
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred in " + strClassName + ": " + ex.getMessage());
		}
		Log.printVerbose("***** END: FIX -VE OUTSTANDING INVOICE *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "FIX -VE OUTSTANDING INVOICE");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		// Clean up curTask;
		curTask = null;
		return new ActionRouter("test-migrate-topcondb-page");
	}

	public String getOldDocNoFromRemarks(String remarks)
	{
		String[] arrSplit = remarks.split(" = ");
		System.out.println(Arrays.asList(arrSplit));
		String ans = arrSplit[1].trim().substring(0, arrSplit[1].trim().indexOf(')'));
		System.out.println("Ans: " + ans);
		return ans;
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

/**
 * 
 * DoFixNegOutstandingInv ====================== During Topcon's DB migration,
 * there had been some duplicate entries that has led to negative invoice
 * balances. To fix this, the following algorithm is proposed:
 * 
 * for each invoice with -ve outstanding bal, 1. select remarks from
 * cust_invoice_index where invoice_pkid=<outstanding_inv_pkid> and parse it to
 * obtain the Old Docno 2. select (ldebitamt-lcreditamt) from cdhis where docno=<Old
 * DocNo> = corrected_outstanding_amt 3. update cust_invoice_index set
 * outstanding_amt=<corrected_outstanding_amt>
 * 
 */
public class DoFixNegOutstandingInv implements Action
{
	private String strClassName = "DoFixNegOutstandingInv";
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
			return new ActionRouter("test-migrate-topcondb-page");
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
			// Select all invoices with -ve balance
			String sltInvWithNegBal = "select pkid, outstanding_amt, remarks from cust_invoice_index where outstanding_amt < 0";
			ResultSet rs = empStmt.executeQuery(sltInvWithNegBal);
			int count = 1;
			while (rs.next())
			{
				Log.printDebug("** Processing Entry: " + count++);
				Long lPkid = new Long(rs.getLong("pkid"));
				BigDecimal bdOutstandingAmt = rs.getBigDecimal("outstanding_amt");
				String sRemarks = rs.getString("remarks");
				// Parse the remarks to obtain the old docNo
				String oldDocNo = getOldDocNoFromRemarks(sRemarks);
				if (oldDocNo == null || oldDocNo.equals(""))
				{
					Log.printDebug("*** Failed to get DocNo from remarks: " + sRemarks);
					continue;
				}
				// select correct balance from topcon DB
				String sltBalFromCdHis = "select (ldebitamt-lcreditamt) as bal from cdhis where docno = '" + oldDocNo
						+ "'";
				ResultSet rsBal = topconStmt.executeQuery(sltBalFromCdHis);
				if (rsBal.next())
				{
					BigDecimal correctBal = rsBal.getBigDecimal("bal");
					// Update emp DB with correct bal
					Invoice inv = InvoiceNut.getHandle(lPkid);
					if (inv != null)
					{
						inv.setOutstandingAmt(correctBal);
						Log.printVerbose("Successfully set new outstanding_amt for Invoice " + lPkid + " to "
								+ correctBal);
					}
				}
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred in " + strClassName + ": " + ex.getMessage());
		}
		Log.printVerbose("***** END: FIX -VE OUTSTANDING INVOICE *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "FIX -VE OUTSTANDING INVOICE");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		// Clean up curTask;
		curTask = null;
		return new ActionRouter("test-migrate-topcondb-page");
	}

	public String getOldDocNoFromRemarks(String remarks)
	{
		String[] arrSplit = remarks.split(" = ");
		System.out.println(Arrays.asList(arrSplit));
		String ans = arrSplit[1].trim().substring(0, arrSplit[1].trim().indexOf(')'));
		System.out.println("Ans: " + ans);
		return ans;
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

/**
 * 
 * DoFixNegOutstandingInv ====================== During Topcon's DB migration,
 * there had been some duplicate entries that has led to negative invoice
 * balances. To fix this, the following algorithm is proposed:
 * 
 * for each invoice with -ve outstanding bal, 1. select remarks from
 * cust_invoice_index where invoice_pkid=<outstanding_inv_pkid> and parse it to
 * obtain the Old Docno 2. select (ldebitamt-lcreditamt) from cdhis where docno=<Old
 * DocNo> = corrected_outstanding_amt 3. update cust_invoice_index set
 * outstanding_amt=<corrected_outstanding_amt>
 * 
 */
public class DoFixNegOutstandingInv implements Action
{
	private String strClassName = "DoFixNegOutstandingInv";
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
			return new ActionRouter("test-migrate-topcondb-page");
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
			// Select all invoices with -ve balance
			String sltInvWithNegBal = "select pkid, outstanding_amt, remarks from cust_invoice_index where outstanding_amt < 0";
			ResultSet rs = empStmt.executeQuery(sltInvWithNegBal);
			int count = 1;
			while (rs.next())
			{
				Log.printDebug("** Processing Entry: " + count++);
				Long lPkid = new Long(rs.getLong("pkid"));
				BigDecimal bdOutstandingAmt = rs.getBigDecimal("outstanding_amt");
				String sRemarks = rs.getString("remarks");
				// Parse the remarks to obtain the old docNo
				String oldDocNo = getOldDocNoFromRemarks(sRemarks);
				if (oldDocNo == null || oldDocNo.equals(""))
				{
					Log.printDebug("*** Failed to get DocNo from remarks: " + sRemarks);
					continue;
				}
				// select correct balance from topcon DB
				String sltBalFromCdHis = "select (ldebitamt-lcreditamt) as bal from cdhis where docno = '" + oldDocNo
						+ "'";
				ResultSet rsBal = topconStmt.executeQuery(sltBalFromCdHis);
				if (rsBal.next())
				{
					BigDecimal correctBal = rsBal.getBigDecimal("bal");
					// Update emp DB with correct bal
					Invoice inv = InvoiceNut.getHandle(lPkid);
					if (inv != null)
					{
						inv.setOutstandingAmt(correctBal);
						Log.printVerbose("Successfully set new outstanding_amt for Invoice " + lPkid + " to "
								+ correctBal);
					}
				}
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred in " + strClassName + ": " + ex.getMessage());
		}
		Log.printVerbose("***** END: FIX -VE OUTSTANDING INVOICE *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "FIX -VE OUTSTANDING INVOICE");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		// Clean up curTask;
		curTask = null;
		return new ActionRouter("test-migrate-topcondb-page");
	}

	public String getOldDocNoFromRemarks(String remarks)
	{
		String[] arrSplit = remarks.split(" = ");
		System.out.println(Arrays.asList(arrSplit));
		String ans = arrSplit[1].trim().substring(0, arrSplit[1].trim().indexOf(')'));
		System.out.println("Ans: " + ans);
		return ans;
	}
}
