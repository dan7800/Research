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

public class DoMigrateSalespersonTerritoryID implements Action
{
	private String strClassName = "DoMigrateSalespersonTerritoryID";
	private static Task curTask = null;
	// Constants
	private static Integer iDefLocId = new Integer(1000);
	private static Integer iDefSvcCtrId = new Integer(1);
	private static Integer iDefPCCenterId = new Integer(1000);
	private static Integer iDefCondId = new Integer(StockNut.STK_COND_GOOD);
	private static Integer iDefCashAccId = new Integer(1000);
	private static Integer iDefChequeAccId = new Integer(1001);
	private BranchObject defBranchObj = null;

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
				String terrId = rsCust.getString("terrid");
				String srId = rsCust.getString("srid");
				// Create the custAccount
				CustAccount theCustAccEJB = CustAccountNut.getObjectByCode(custId);
				if (theCustAccEJB != null)
				{
					try
					{
						CustAccountObject custAccObj = theCustAccEJB.getObject();
						Integer theUserIdOfSalesman = UserNut.getUserId(srId);
						if (theUserIdOfSalesman != null)
						{
							custAccObj.salesman = theUserIdOfSalesman;
							custAccObj.property5 = terrId;
							theCustAccEJB.setObject(custAccObj);
						}
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
			custStmt.close();
			/*******************************************************************
			 * FIXES AFTER POPULATION OF DOC INDICES
			 ******************************************************************/
			// 1. Fix all transactions that had gainloseamt != 0 owing to
			// differing exch_rates
			// fixExRate(con, jbossCon, usrid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while Migrating Territory and SalesPerson Id: "
					+ ex.getMessage());
		}
		Log.printVerbose("***** END: MIGRATION OF SALESPERSON AND TERRITORY DB *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "CREATE SALES PERSON AND TERRITORY");
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
import java.io.Serializable;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;

public class DoMigrateSalespersonTerritoryID implements Action
{
	private String strClassName = "DoMigrateSalespersonTerritoryID";
	private static Task curTask = null;
	// Constants
	private static Integer iDefLocId = new Integer(1000);
	private static Integer iDefSvcCtrId = new Integer(1);
	private static Integer iDefPCCenterId = new Integer(1000);
	private static Integer iDefCondId = new Integer(StockNut.STK_COND_GOOD);
	private static Integer iDefCashAccId = new Integer(1000);
	private static Integer iDefChequeAccId = new Integer(1001);
	private BranchObject defBranchObj = null;

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
				String terrId = rsCust.getString("terrid");
				String srId = rsCust.getString("srid");
				// Create the custAccount
				CustAccount theCustAccEJB = CustAccountNut.getObjectByCode(custId);
				if (theCustAccEJB != null)
				{
					try
					{
						CustAccountObject custAccObj = theCustAccEJB.getObject();
						Integer theUserIdOfSalesman = UserNut.getUserId(srId);
						if (theUserIdOfSalesman != null)
						{
							custAccObj.salesman = theUserIdOfSalesman;
							custAccObj.property5 = terrId;
							theCustAccEJB.setObject(custAccObj);
						}
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
			custStmt.close();
			/*******************************************************************
			 * FIXES AFTER POPULATION OF DOC INDICES
			 ******************************************************************/
			// 1. Fix all transactions that had gainloseamt != 0 owing to
			// differing exch_rates
			// fixExRate(con, jbossCon, usrid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while Migrating Territory and SalesPerson Id: "
					+ ex.getMessage());
		}
		Log.printVerbose("***** END: MIGRATION OF SALESPERSON AND TERRITORY DB *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "CREATE SALES PERSON AND TERRITORY");
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
import java.io.Serializable;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;

public class DoMigrateSalespersonTerritoryID implements Action
{
	private String strClassName = "DoMigrateSalespersonTerritoryID";
	private static Task curTask = null;
	// Constants
	private static Integer iDefLocId = new Integer(1000);
	private static Integer iDefSvcCtrId = new Integer(1);
	private static Integer iDefPCCenterId = new Integer(1000);
	private static Integer iDefCondId = new Integer(StockNut.STK_COND_GOOD);
	private static Integer iDefCashAccId = new Integer(1000);
	private static Integer iDefChequeAccId = new Integer(1001);
	private BranchObject defBranchObj = null;

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
				String terrId = rsCust.getString("terrid");
				String srId = rsCust.getString("srid");
				// Create the custAccount
				CustAccount theCustAccEJB = CustAccountNut.getObjectByCode(custId);
				if (theCustAccEJB != null)
				{
					try
					{
						CustAccountObject custAccObj = theCustAccEJB.getObject();
						Integer theUserIdOfSalesman = UserNut.getUserId(srId);
						if (theUserIdOfSalesman != null)
						{
							custAccObj.salesman = theUserIdOfSalesman;
							custAccObj.property5 = terrId;
							theCustAccEJB.setObject(custAccObj);
						}
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
			custStmt.close();
			/*******************************************************************
			 * FIXES AFTER POPULATION OF DOC INDICES
			 ******************************************************************/
			// 1. Fix all transactions that had gainloseamt != 0 owing to
			// differing exch_rates
			// fixExRate(con, jbossCon, usrid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while Migrating Territory and SalesPerson Id: "
					+ ex.getMessage());
		}
		Log.printVerbose("***** END: MIGRATION OF SALESPERSON AND TERRITORY DB *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "CREATE SALES PERSON AND TERRITORY");
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
import java.io.Serializable;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;

public class DoMigrateSalespersonTerritoryID implements Action
{
	private String strClassName = "DoMigrateSalespersonTerritoryID";
	private static Task curTask = null;
	// Constants
	private static Integer iDefLocId = new Integer(1000);
	private static Integer iDefSvcCtrId = new Integer(1);
	private static Integer iDefPCCenterId = new Integer(1000);
	private static Integer iDefCondId = new Integer(StockNut.STK_COND_GOOD);
	private static Integer iDefCashAccId = new Integer(1000);
	private static Integer iDefChequeAccId = new Integer(1001);
	private BranchObject defBranchObj = null;

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
				String terrId = rsCust.getString("terrid");
				String srId = rsCust.getString("srid");
				// Create the custAccount
				CustAccount theCustAccEJB = CustAccountNut.getObjectByCode(custId);
				if (theCustAccEJB != null)
				{
					try
					{
						CustAccountObject custAccObj = theCustAccEJB.getObject();
						Integer theUserIdOfSalesman = UserNut.getUserId(srId);
						if (theUserIdOfSalesman != null)
						{
							custAccObj.salesman = theUserIdOfSalesman;
							custAccObj.property5 = terrId;
							theCustAccEJB.setObject(custAccObj);
						}
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
			custStmt.close();
			/*******************************************************************
			 * FIXES AFTER POPULATION OF DOC INDICES
			 ******************************************************************/
			// 1. Fix all transactions that had gainloseamt != 0 owing to
			// differing exch_rates
			// fixExRate(con, jbossCon, usrid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while Migrating Territory and SalesPerson Id: "
					+ ex.getMessage());
		}
		Log.printVerbose("***** END: MIGRATION OF SALESPERSON AND TERRITORY DB *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "CREATE SALES PERSON AND TERRITORY");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		// Clean up curTask;
		curTask = null;
		return new ActionRouter("test-migrate-topcondb-page");
	}
}
