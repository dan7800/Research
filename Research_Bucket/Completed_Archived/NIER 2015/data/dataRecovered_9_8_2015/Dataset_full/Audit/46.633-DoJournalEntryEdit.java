package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoJournalEntryEdit implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		/*
		 * // Form Hnalders String formName = req.getParameter("formName");
		 * if(formName == null) { fnGetJournalEntryList(servlet, req, res);
		 * return new ActionRouter("acc-edit-journalentry-page"); }
		 * 
		 * if(formName.equals("addJournalEntry")) { fnAddJournalEntry(servlet,
		 * req, res); }
		 * 
		 * if(formName.equals("rmJournalEntry")) { fnRmJournalEntry(servlet,
		 * req, res); }
		 * 
		 * fnGetJournalEntryList(servlet, req, res);
		 */return new ActionRouter("acc-edit-journalentry-page");
	}
	/*
	 * protected void fnRmJournalEntry(HttpServlet servlet, HttpServletRequest
	 * req, HttpServletResponse res) { Long rmJournalEntryId= new Long(
	 * req.getParameter("removeJournalEntry")); if(rmJournalEntryId != null) {
	 * JournalEntry lJEntry = JournalEntryNut.getHandle(rmJournalEntryId);
	 * if(lJEntry !=null) { try { lJEntry.remove();} catch(Exception ex) {
	 * Log.printDebug("Remove JournalEntry Failed" + ex.getMessage()); } } } }
	 * 
	 * protected void fnGetJournalEntryList(HttpServlet servlet,
	 * HttpServletRequest req, HttpServletResponse res) { Collection colJEntry =
	 * JournalEntryNut.getAllObjects(); Iterator itrAllJEntry =
	 * colJEntry.iterator(); req.setAttribute("itrAllJEntry", itrAllJEntry); }
	 * 
	 * protected void fnAddJournalEntry(HttpServlet servlet, HttpServletRequest
	 * req, HttpServletResponse res) { Long journalEntryTxnId = new Long(
	 * req.getParameter("journalEntryTxnId")); Integer journalEntryGLId = new
	 * Integer( req.getParameter("journalEntryGLId")); String
	 * journalEntryDebitCredit = (String)
	 * req.getParameter("journalEntryDebitCredit"); Integer journalEntryPCCenter =
	 * new Integer( req.getParameter("journalEntryPCCenterId")); String
	 * journalEntryCcy = (String) req.getParameter("journalEntryCcy");
	 * BigDecimal journalEntryAmt = new BigDecimal(
	 * req.getParameter("journalEntryAmt")); String journalEntryFxPair =
	 * (String) req.getParameter("journalEntryFxPair"); BigDecimal
	 * journalEntryFxRate = new BigDecimal(
	 * req.getParameter("journalEntryFxRate"));
	 * 
	 * HttpSession session = req.getSession(); User lusr = UserNut.getHandle(
	 * (String)session.getAttribute("userName"));
	 * 
	 * JournalEntry lJnlEntry = null;
	 * 
	 * if(lJnlEntry == null && lusr != null) { Log.printVerbose("Adding new
	 * JournalEntry"); JournalEntryHome lJEntryH = JournalEntryNut.getHome();
	 * java.util.Date ldt = new java.util.Date(); Timestamp tsCreate = new
	 * Timestamp(ldt.getTime()); Integer usrid = null; try { usrid =
	 * lusr.getUserId();} catch(Exception ex) { Log.printAudit("User does not
	 * exist: " + ex.getMessage()); }
	 * 
	 * try { JournalEntry newJEntry =
	 * (JournalEntry)lJEntryH.create(journalEntryGLId, journalEntryDebitCredit,
	 * journalEntryPCCenter, journalEntryTxnId, journalEntryCcy,
	 * journalEntryAmt, journalEntryFxPair, journalEntryFxRate); }
	 * catch(Exception ex) { Log.printDebug("Cannot create JournalEntry " +
	 * ex.getMessage()); } }
	 * 
	 * fnGetJournalEntryList(servlet, req, res); }
	 */
}
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoJournalEntryEdit implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		/*
		 * // Form Hnalders String formName = req.getParameter("formName");
		 * if(formName == null) { fnGetJournalEntryList(servlet, req, res);
		 * return new ActionRouter("acc-edit-journalentry-page"); }
		 * 
		 * if(formName.equals("addJournalEntry")) { fnAddJournalEntry(servlet,
		 * req, res); }
		 * 
		 * if(formName.equals("rmJournalEntry")) { fnRmJournalEntry(servlet,
		 * req, res); }
		 * 
		 * fnGetJournalEntryList(servlet, req, res);
		 */return new ActionRouter("acc-edit-journalentry-page");
	}
	/*
	 * protected void fnRmJournalEntry(HttpServlet servlet, HttpServletRequest
	 * req, HttpServletResponse res) { Long rmJournalEntryId= new Long(
	 * req.getParameter("removeJournalEntry")); if(rmJournalEntryId != null) {
	 * JournalEntry lJEntry = JournalEntryNut.getHandle(rmJournalEntryId);
	 * if(lJEntry !=null) { try { lJEntry.remove();} catch(Exception ex) {
	 * Log.printDebug("Remove JournalEntry Failed" + ex.getMessage()); } } } }
	 * 
	 * protected void fnGetJournalEntryList(HttpServlet servlet,
	 * HttpServletRequest req, HttpServletResponse res) { Collection colJEntry =
	 * JournalEntryNut.getAllObjects(); Iterator itrAllJEntry =
	 * colJEntry.iterator(); req.setAttribute("itrAllJEntry", itrAllJEntry); }
	 * 
	 * protected void fnAddJournalEntry(HttpServlet servlet, HttpServletRequest
	 * req, HttpServletResponse res) { Long journalEntryTxnId = new Long(
	 * req.getParameter("journalEntryTxnId")); Integer journalEntryGLId = new
	 * Integer( req.getParameter("journalEntryGLId")); String
	 * journalEntryDebitCredit = (String)
	 * req.getParameter("journalEntryDebitCredit"); Integer journalEntryPCCenter =
	 * new Integer( req.getParameter("journalEntryPCCenterId")); String
	 * journalEntryCcy = (String) req.getParameter("journalEntryCcy");
	 * BigDecimal journalEntryAmt = new BigDecimal(
	 * req.getParameter("journalEntryAmt")); String journalEntryFxPair =
	 * (String) req.getParameter("journalEntryFxPair"); BigDecimal
	 * journalEntryFxRate = new BigDecimal(
	 * req.getParameter("journalEntryFxRate"));
	 * 
	 * HttpSession session = req.getSession(); User lusr = UserNut.getHandle(
	 * (String)session.getAttribute("userName"));
	 * 
	 * JournalEntry lJnlEntry = null;
	 * 
	 * if(lJnlEntry == null && lusr != null) { Log.printVerbose("Adding new
	 * JournalEntry"); JournalEntryHome lJEntryH = JournalEntryNut.getHome();
	 * java.util.Date ldt = new java.util.Date(); Timestamp tsCreate = new
	 * Timestamp(ldt.getTime()); Integer usrid = null; try { usrid =
	 * lusr.getUserId();} catch(Exception ex) { Log.printAudit("User does not
	 * exist: " + ex.getMessage()); }
	 * 
	 * try { JournalEntry newJEntry =
	 * (JournalEntry)lJEntryH.create(journalEntryGLId, journalEntryDebitCredit,
	 * journalEntryPCCenter, journalEntryTxnId, journalEntryCcy,
	 * journalEntryAmt, journalEntryFxPair, journalEntryFxRate); }
	 * catch(Exception ex) { Log.printDebug("Cannot create JournalEntry " +
	 * ex.getMessage()); } }
	 * 
	 * fnGetJournalEntryList(servlet, req, res); }
	 */
}
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoJournalEntryEdit implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		/*
		 * // Form Hnalders String formName = req.getParameter("formName");
		 * if(formName == null) { fnGetJournalEntryList(servlet, req, res);
		 * return new ActionRouter("acc-edit-journalentry-page"); }
		 * 
		 * if(formName.equals("addJournalEntry")) { fnAddJournalEntry(servlet,
		 * req, res); }
		 * 
		 * if(formName.equals("rmJournalEntry")) { fnRmJournalEntry(servlet,
		 * req, res); }
		 * 
		 * fnGetJournalEntryList(servlet, req, res);
		 */return new ActionRouter("acc-edit-journalentry-page");
	}
	/*
	 * protected void fnRmJournalEntry(HttpServlet servlet, HttpServletRequest
	 * req, HttpServletResponse res) { Long rmJournalEntryId= new Long(
	 * req.getParameter("removeJournalEntry")); if(rmJournalEntryId != null) {
	 * JournalEntry lJEntry = JournalEntryNut.getHandle(rmJournalEntryId);
	 * if(lJEntry !=null) { try { lJEntry.remove();} catch(Exception ex) {
	 * Log.printDebug("Remove JournalEntry Failed" + ex.getMessage()); } } } }
	 * 
	 * protected void fnGetJournalEntryList(HttpServlet servlet,
	 * HttpServletRequest req, HttpServletResponse res) { Collection colJEntry =
	 * JournalEntryNut.getAllObjects(); Iterator itrAllJEntry =
	 * colJEntry.iterator(); req.setAttribute("itrAllJEntry", itrAllJEntry); }
	 * 
	 * protected void fnAddJournalEntry(HttpServlet servlet, HttpServletRequest
	 * req, HttpServletResponse res) { Long journalEntryTxnId = new Long(
	 * req.getParameter("journalEntryTxnId")); Integer journalEntryGLId = new
	 * Integer( req.getParameter("journalEntryGLId")); String
	 * journalEntryDebitCredit = (String)
	 * req.getParameter("journalEntryDebitCredit"); Integer journalEntryPCCenter =
	 * new Integer( req.getParameter("journalEntryPCCenterId")); String
	 * journalEntryCcy = (String) req.getParameter("journalEntryCcy");
	 * BigDecimal journalEntryAmt = new BigDecimal(
	 * req.getParameter("journalEntryAmt")); String journalEntryFxPair =
	 * (String) req.getParameter("journalEntryFxPair"); BigDecimal
	 * journalEntryFxRate = new BigDecimal(
	 * req.getParameter("journalEntryFxRate"));
	 * 
	 * HttpSession session = req.getSession(); User lusr = UserNut.getHandle(
	 * (String)session.getAttribute("userName"));
	 * 
	 * JournalEntry lJnlEntry = null;
	 * 
	 * if(lJnlEntry == null && lusr != null) { Log.printVerbose("Adding new
	 * JournalEntry"); JournalEntryHome lJEntryH = JournalEntryNut.getHome();
	 * java.util.Date ldt = new java.util.Date(); Timestamp tsCreate = new
	 * Timestamp(ldt.getTime()); Integer usrid = null; try { usrid =
	 * lusr.getUserId();} catch(Exception ex) { Log.printAudit("User does not
	 * exist: " + ex.getMessage()); }
	 * 
	 * try { JournalEntry newJEntry =
	 * (JournalEntry)lJEntryH.create(journalEntryGLId, journalEntryDebitCredit,
	 * journalEntryPCCenter, journalEntryTxnId, journalEntryCcy,
	 * journalEntryAmt, journalEntryFxPair, journalEntryFxRate); }
	 * catch(Exception ex) { Log.printDebug("Cannot create JournalEntry " +
	 * ex.getMessage()); } }
	 * 
	 * fnGetJournalEntryList(servlet, req, res); }
	 */
}
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoJournalEntryEdit implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		/*
		 * // Form Hnalders String formName = req.getParameter("formName");
		 * if(formName == null) { fnGetJournalEntryList(servlet, req, res);
		 * return new ActionRouter("acc-edit-journalentry-page"); }
		 * 
		 * if(formName.equals("addJournalEntry")) { fnAddJournalEntry(servlet,
		 * req, res); }
		 * 
		 * if(formName.equals("rmJournalEntry")) { fnRmJournalEntry(servlet,
		 * req, res); }
		 * 
		 * fnGetJournalEntryList(servlet, req, res);
		 */return new ActionRouter("acc-edit-journalentry-page");
	}
	/*
	 * protected void fnRmJournalEntry(HttpServlet servlet, HttpServletRequest
	 * req, HttpServletResponse res) { Long rmJournalEntryId= new Long(
	 * req.getParameter("removeJournalEntry")); if(rmJournalEntryId != null) {
	 * JournalEntry lJEntry = JournalEntryNut.getHandle(rmJournalEntryId);
	 * if(lJEntry !=null) { try { lJEntry.remove();} catch(Exception ex) {
	 * Log.printDebug("Remove JournalEntry Failed" + ex.getMessage()); } } } }
	 * 
	 * protected void fnGetJournalEntryList(HttpServlet servlet,
	 * HttpServletRequest req, HttpServletResponse res) { Collection colJEntry =
	 * JournalEntryNut.getAllObjects(); Iterator itrAllJEntry =
	 * colJEntry.iterator(); req.setAttribute("itrAllJEntry", itrAllJEntry); }
	 * 
	 * protected void fnAddJournalEntry(HttpServlet servlet, HttpServletRequest
	 * req, HttpServletResponse res) { Long journalEntryTxnId = new Long(
	 * req.getParameter("journalEntryTxnId")); Integer journalEntryGLId = new
	 * Integer( req.getParameter("journalEntryGLId")); String
	 * journalEntryDebitCredit = (String)
	 * req.getParameter("journalEntryDebitCredit"); Integer journalEntryPCCenter =
	 * new Integer( req.getParameter("journalEntryPCCenterId")); String
	 * journalEntryCcy = (String) req.getParameter("journalEntryCcy");
	 * BigDecimal journalEntryAmt = new BigDecimal(
	 * req.getParameter("journalEntryAmt")); String journalEntryFxPair =
	 * (String) req.getParameter("journalEntryFxPair"); BigDecimal
	 * journalEntryFxRate = new BigDecimal(
	 * req.getParameter("journalEntryFxRate"));
	 * 
	 * HttpSession session = req.getSession(); User lusr = UserNut.getHandle(
	 * (String)session.getAttribute("userName"));
	 * 
	 * JournalEntry lJnlEntry = null;
	 * 
	 * if(lJnlEntry == null && lusr != null) { Log.printVerbose("Adding new
	 * JournalEntry"); JournalEntryHome lJEntryH = JournalEntryNut.getHome();
	 * java.util.Date ldt = new java.util.Date(); Timestamp tsCreate = new
	 * Timestamp(ldt.getTime()); Integer usrid = null; try { usrid =
	 * lusr.getUserId();} catch(Exception ex) { Log.printAudit("User does not
	 * exist: " + ex.getMessage()); }
	 * 
	 * try { JournalEntry newJEntry =
	 * (JournalEntry)lJEntryH.create(journalEntryGLId, journalEntryDebitCredit,
	 * journalEntryPCCenter, journalEntryTxnId, journalEntryCcy,
	 * journalEntryAmt, journalEntryFxPair, journalEntryFxRate); }
	 * catch(Exception ex) { Log.printDebug("Cannot create JournalEntry " +
	 * ex.getMessage()); } }
	 * 
	 * fnGetJournalEntryList(servlet, req, res); }
	 */
}
