/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.management;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.accounting.*;

public class DoMgtMonthlyDebtorCollectionReportType01 implements Action
{
	String strClassName = "DoMgtMonthlyDebtorCollectionReportType01";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		String fwdPage = (String) req.getParameter("fwdPage");
		Vector vecPCCenter = ProfitCostCenterNut.getValueObjectsGiven(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.STATUS_ACTIVE, (String) null, (String) null);
		req.setAttribute("vecPCCenter", vecPCCenter);
		fnGetReportType01Values(servlet, req, res);
		if (fwdPage != null)
		{
			return new ActionRouter(fwdPage);
		}
		return new ActionRouter("mgt-monthly-debtor-collection-report-type01-page");
	}

	public void fnGetReportType01Values(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		try
		{
			Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
			String strPCCenter = req.getParameter("pcCenter");
			req.setAttribute("strPCCenter", strPCCenter);
			String currency = req.getParameter("currency");
			req.setAttribute("currency", currency);
			String strDateFrom = req.getParameter("dateFrom");
			req.setAttribute("dateFrom", strDateFrom);
			String strDateTo = req.getParameter("dateTo");
			req.setAttribute("dateTo", strDateTo);
			if (strPCCenter == null || currency == null || strDateFrom == null)
			{
				return;
			}
			Integer pcCenter = null;
			try
			{
				pcCenter = new Integer(strPCCenter);
			} catch (Exception ex)
			{ // do nothing, when "all" is selected
			}
			// // NominalAccount ForeignTable
			// /// NOT NominalAccountTxn ForeignTable
			String naForeignTable = NominalAccountBean.FT_CUSTOMER;
			Integer naForeignKey = null;// new Integer("0");
			Long natForeignKey = null;
			String natForeignTable = null;// =
											// NominalAccountTxnBean.FT_CUST_INVOICE;
			Vector vecInvRecExternal = new Vector();
			Vector vecInvRecInternal = new Vector();
			Vector vecRecOnlyExternal = new Vector();
			Vector vecRecOnlyInternal = new Vector();
			// / count number of days
			Timestamp dateFrom = TimeFormat.createTimestamp(strDateFrom);
			Timestamp dateTo = TimeFormat.createTimestamp(strDateTo);
			Timestamp dateRun = TimeFormat.createTimestamp(strDateFrom);
			int nDays = 0;
			while (dateRun.getTime() < dateTo.getTime())
			{
				nDays += 1;
				dateRun = TimeFormat.add(dateFrom, 0, 0, nDays);
				Log.printVerbose(" dateRun = " + dateRun.toString());
				Log.printVerbose(" nDays = " + nDays);
			}
			Log.printVerbose(" ---------------------------- ");
			Log.printVerbose(" dateFrom = " + dateFrom.toString());
			Log.printVerbose(" dateTo = " + dateTo.toString());
			Log.printVerbose(" nDays = " + nDays);
			// ////////////////////////////////////////////////////////////
			// start of for loop
			for (int cDay = 0; cDay < nDays; cDay++)
			{
				Timestamp tsFrom = TimeFormat.add(dateFrom, 0, 0, cDay);
				Timestamp tsTo = TimeFormat.add(tsFrom, 0, 0, 1);
				Log.printVerbose("tsFrom = " + tsFrom.toString());
				Log.printVerbose("tsTo = " + tsTo.toString());
				String strOption = "active";
				// / first, retrieve all invoices posted to nominal account
				// / on a particular day
				natForeignTable = NominalAccountTxnBean.FT_CUST_INVOICE;
				Vector vecNaInv = NominalAccountNut.getValueObjectsGiven(pcCenter, naForeignTable, naForeignKey,
						currency, natForeignTable, natForeignKey, tsFrom, tsTo, strOption);
				// / next, retrieve all receipts posted to nominal account
				// / on a particular day
				Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 2");
				natForeignTable = NominalAccountTxnBean.FT_CUST_RECEIPT;
				Vector vecNaRec = NominalAccountNut.getValueObjectsGiven(pcCenter, naForeignTable, naForeignKey,
						currency, natForeignTable, natForeignKey, tsFrom, tsTo, strOption);
				vecNaInv.addAll(vecNaRec);
				// / next find out if there's duplicate of NominalAccount in the
				// list
				// / above, if there are, merge them
				Vector vecMerged = NominalAccountNut.fnMergeNominalAccountWithSameID(vecNaInv);
				Vector vecInvRec = new Vector();
				Vector vecRecOnly = new Vector();
				Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 3");
				for (int count1 = 0; count1 < vecMerged.size(); count1++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecMerged.get(count1);
					BigDecimal bdTotalInvoice = new BigDecimal("0.00");
					BigDecimal bdTotalReceipt = new BigDecimal("0.00");
					Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 4");
					for (int count2 = 0; count2 < naObj.vecNominalAccountTxn.size(); count2++)
					{
						NominalAccountTxnObject natObj = (NominalAccountTxnObject) naObj.vecNominalAccountTxn
								.get(count2);
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_INVOICE))
						{
							Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
							bdTotalInvoice = bdTotalInvoice.add(natObj.amount);
						} else if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_RECEIPT))
						{
							Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
							bdTotalReceipt = bdTotalReceipt.add(natObj.amount);
						}
					}
					if (bdTotalInvoice.signum() == 0)
					{
						// receipts only vector
						vecRecOnly.add(naObj);
						Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
					} else
					{
						vecInvRec.add(naObj);
						Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
					}
				}
				/*
				 * for(int count1=0;count1<vecInvRec.size();count1++) {
				 * NominalAccountObject naObj = (NominalAccountObject)
				 * vecInvRec.get(count1);
				 * if(naObj.foreignTable.equals(NominalAccountBean.FT_CUSTOMER)) {
				 * try { CustAccount caEJB =
				 * CustAccountNut.getHandle(naObj.foreignKey);
				 * Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
				 * 
				 * Integer iType = caEJB.getAccType(); if(iType.intValue()==
				 * CustAccountBean.ACCTYPE_INTERNAL_ENUM.intValue()) {
				 * vecInvRecInternal.add(naObj);} else {
				 * vecInvRecExternal.add(naObj);} } catch(Exception ex) {
				 * ex.printStackTrace(); } } }
				 * 
				 */
				for (int count1 = 0; count1 < vecRecOnly.size(); count1++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecRecOnly.get(count1);
					if (naObj.foreignTable.equals(NominalAccountBean.FT_CUSTOMER))
					{
						Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
						try
						{
							CustAccount caEJB = CustAccountNut.getHandle(naObj.foreignKey);
							Integer iType = caEJB.getAccType();
							if (iType.intValue() == CustAccountBean.ACCTYPE_CORPORATE.intValue())
							{
								vecRecOnlyInternal.add(naObj);
							} else
							{
								vecRecOnlyExternal.add(naObj);
							}
						} catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
				}
			}// / end of for cDay
			// / next, split this vector into internal/external vector
			// / base on customer type
			Log.printVerbose(" after calling na nut to retrieve na txns !!");
			req.setAttribute("vecInvRecInternal", vecInvRecInternal);// empty
			req.setAttribute("vecInvRecExternal", vecInvRecExternal);// empty
			req.setAttribute("vecRecOnlyExternal", vecRecOnlyExternal);
			req.setAttribute("vecRecOnlyInternal", vecRecOnlyInternal);
			fnAuditTrail(servlet, req, res);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "mgt_report: monthly-debtors-collection-report";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}// end of DoMgtMonthlyDebtorCollectionReportType01
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.management;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.accounting.*;

public class DoMgtMonthlyDebtorCollectionReportType01 implements Action
{
	String strClassName = "DoMgtMonthlyDebtorCollectionReportType01";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		String fwdPage = (String) req.getParameter("fwdPage");
		Vector vecPCCenter = ProfitCostCenterNut.getValueObjectsGiven(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.STATUS_ACTIVE, (String) null, (String) null);
		req.setAttribute("vecPCCenter", vecPCCenter);
		fnGetReportType01Values(servlet, req, res);
		if (fwdPage != null)
		{
			return new ActionRouter(fwdPage);
		}
		return new ActionRouter("mgt-monthly-debtor-collection-report-type01-page");
	}

	public void fnGetReportType01Values(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		try
		{
			Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
			String strPCCenter = req.getParameter("pcCenter");
			req.setAttribute("strPCCenter", strPCCenter);
			String currency = req.getParameter("currency");
			req.setAttribute("currency", currency);
			String strDateFrom = req.getParameter("dateFrom");
			req.setAttribute("dateFrom", strDateFrom);
			String strDateTo = req.getParameter("dateTo");
			req.setAttribute("dateTo", strDateTo);
			if (strPCCenter == null || currency == null || strDateFrom == null)
			{
				return;
			}
			Integer pcCenter = null;
			try
			{
				pcCenter = new Integer(strPCCenter);
			} catch (Exception ex)
			{ // do nothing, when "all" is selected
			}
			// // NominalAccount ForeignTable
			// /// NOT NominalAccountTxn ForeignTable
			String naForeignTable = NominalAccountBean.FT_CUSTOMER;
			Integer naForeignKey = null;// new Integer("0");
			Long natForeignKey = null;
			String natForeignTable = null;// =
											// NominalAccountTxnBean.FT_CUST_INVOICE;
			Vector vecInvRecExternal = new Vector();
			Vector vecInvRecInternal = new Vector();
			Vector vecRecOnlyExternal = new Vector();
			Vector vecRecOnlyInternal = new Vector();
			// / count number of days
			Timestamp dateFrom = TimeFormat.createTimestamp(strDateFrom);
			Timestamp dateTo = TimeFormat.createTimestamp(strDateTo);
			Timestamp dateRun = TimeFormat.createTimestamp(strDateFrom);
			int nDays = 0;
			while (dateRun.getTime() < dateTo.getTime())
			{
				nDays += 1;
				dateRun = TimeFormat.add(dateFrom, 0, 0, nDays);
				Log.printVerbose(" dateRun = " + dateRun.toString());
				Log.printVerbose(" nDays = " + nDays);
			}
			Log.printVerbose(" ---------------------------- ");
			Log.printVerbose(" dateFrom = " + dateFrom.toString());
			Log.printVerbose(" dateTo = " + dateTo.toString());
			Log.printVerbose(" nDays = " + nDays);
			// ////////////////////////////////////////////////////////////
			// start of for loop
			for (int cDay = 0; cDay < nDays; cDay++)
			{
				Timestamp tsFrom = TimeFormat.add(dateFrom, 0, 0, cDay);
				Timestamp tsTo = TimeFormat.add(tsFrom, 0, 0, 1);
				Log.printVerbose("tsFrom = " + tsFrom.toString());
				Log.printVerbose("tsTo = " + tsTo.toString());
				String strOption = "active";
				// / first, retrieve all invoices posted to nominal account
				// / on a particular day
				natForeignTable = NominalAccountTxnBean.FT_CUST_INVOICE;
				Vector vecNaInv = NominalAccountNut.getValueObjectsGiven(pcCenter, naForeignTable, naForeignKey,
						currency, natForeignTable, natForeignKey, tsFrom, tsTo, strOption);
				// / next, retrieve all receipts posted to nominal account
				// / on a particular day
				Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 2");
				natForeignTable = NominalAccountTxnBean.FT_CUST_RECEIPT;
				Vector vecNaRec = NominalAccountNut.getValueObjectsGiven(pcCenter, naForeignTable, naForeignKey,
						currency, natForeignTable, natForeignKey, tsFrom, tsTo, strOption);
				vecNaInv.addAll(vecNaRec);
				// / next find out if there's duplicate of NominalAccount in the
				// list
				// / above, if there are, merge them
				Vector vecMerged = NominalAccountNut.fnMergeNominalAccountWithSameID(vecNaInv);
				Vector vecInvRec = new Vector();
				Vector vecRecOnly = new Vector();
				Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 3");
				for (int count1 = 0; count1 < vecMerged.size(); count1++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecMerged.get(count1);
					BigDecimal bdTotalInvoice = new BigDecimal("0.00");
					BigDecimal bdTotalReceipt = new BigDecimal("0.00");
					Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 4");
					for (int count2 = 0; count2 < naObj.vecNominalAccountTxn.size(); count2++)
					{
						NominalAccountTxnObject natObj = (NominalAccountTxnObject) naObj.vecNominalAccountTxn
								.get(count2);
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_INVOICE))
						{
							Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
							bdTotalInvoice = bdTotalInvoice.add(natObj.amount);
						} else if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_RECEIPT))
						{
							Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
							bdTotalReceipt = bdTotalReceipt.add(natObj.amount);
						}
					}
					if (bdTotalInvoice.signum() == 0)
					{
						// receipts only vector
						vecRecOnly.add(naObj);
						Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
					} else
					{
						vecInvRec.add(naObj);
						Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
					}
				}
				/*
				 * for(int count1=0;count1<vecInvRec.size();count1++) {
				 * NominalAccountObject naObj = (NominalAccountObject)
				 * vecInvRec.get(count1);
				 * if(naObj.foreignTable.equals(NominalAccountBean.FT_CUSTOMER)) {
				 * try { CustAccount caEJB =
				 * CustAccountNut.getHandle(naObj.foreignKey);
				 * Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
				 * 
				 * Integer iType = caEJB.getAccType(); if(iType.intValue()==
				 * CustAccountBean.ACCTYPE_INTERNAL_ENUM.intValue()) {
				 * vecInvRecInternal.add(naObj);} else {
				 * vecInvRecExternal.add(naObj);} } catch(Exception ex) {
				 * ex.printStackTrace(); } } }
				 * 
				 */
				for (int count1 = 0; count1 < vecRecOnly.size(); count1++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecRecOnly.get(count1);
					if (naObj.foreignTable.equals(NominalAccountBean.FT_CUSTOMER))
					{
						Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
						try
						{
							CustAccount caEJB = CustAccountNut.getHandle(naObj.foreignKey);
							Integer iType = caEJB.getAccType();
							if (iType.intValue() == CustAccountBean.ACCTYPE_CORPORATE.intValue())
							{
								vecRecOnlyInternal.add(naObj);
							} else
							{
								vecRecOnlyExternal.add(naObj);
							}
						} catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
				}
			}// / end of for cDay
			// / next, split this vector into internal/external vector
			// / base on customer type
			Log.printVerbose(" after calling na nut to retrieve na txns !!");
			req.setAttribute("vecInvRecInternal", vecInvRecInternal);// empty
			req.setAttribute("vecInvRecExternal", vecInvRecExternal);// empty
			req.setAttribute("vecRecOnlyExternal", vecRecOnlyExternal);
			req.setAttribute("vecRecOnlyInternal", vecRecOnlyInternal);
			fnAuditTrail(servlet, req, res);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "mgt_report: monthly-debtors-collection-report";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}// end of DoMgtMonthlyDebtorCollectionReportType01
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.management;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.accounting.*;

public class DoMgtMonthlyDebtorCollectionReportType01 implements Action
{
	String strClassName = "DoMgtMonthlyDebtorCollectionReportType01";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		String fwdPage = (String) req.getParameter("fwdPage");
		Vector vecPCCenter = ProfitCostCenterNut.getValueObjectsGiven(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.STATUS_ACTIVE, (String) null, (String) null);
		req.setAttribute("vecPCCenter", vecPCCenter);
		fnGetReportType01Values(servlet, req, res);
		if (fwdPage != null)
		{
			return new ActionRouter(fwdPage);
		}
		return new ActionRouter("mgt-monthly-debtor-collection-report-type01-page");
	}

	public void fnGetReportType01Values(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		try
		{
			Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
			String strPCCenter = req.getParameter("pcCenter");
			req.setAttribute("strPCCenter", strPCCenter);
			String currency = req.getParameter("currency");
			req.setAttribute("currency", currency);
			String strDateFrom = req.getParameter("dateFrom");
			req.setAttribute("dateFrom", strDateFrom);
			String strDateTo = req.getParameter("dateTo");
			req.setAttribute("dateTo", strDateTo);
			if (strPCCenter == null || currency == null || strDateFrom == null)
			{
				return;
			}
			Integer pcCenter = null;
			try
			{
				pcCenter = new Integer(strPCCenter);
			} catch (Exception ex)
			{ // do nothing, when "all" is selected
			}
			// // NominalAccount ForeignTable
			// /// NOT NominalAccountTxn ForeignTable
			String naForeignTable = NominalAccountBean.FT_CUSTOMER;
			Integer naForeignKey = null;// new Integer("0");
			Long natForeignKey = null;
			String natForeignTable = null;// =
											// NominalAccountTxnBean.FT_CUST_INVOICE;
			Vector vecInvRecExternal = new Vector();
			Vector vecInvRecInternal = new Vector();
			Vector vecRecOnlyExternal = new Vector();
			Vector vecRecOnlyInternal = new Vector();
			// / count number of days
			Timestamp dateFrom = TimeFormat.createTimestamp(strDateFrom);
			Timestamp dateTo = TimeFormat.createTimestamp(strDateTo);
			Timestamp dateRun = TimeFormat.createTimestamp(strDateFrom);
			int nDays = 0;
			while (dateRun.getTime() < dateTo.getTime())
			{
				nDays += 1;
				dateRun = TimeFormat.add(dateFrom, 0, 0, nDays);
				Log.printVerbose(" dateRun = " + dateRun.toString());
				Log.printVerbose(" nDays = " + nDays);
			}
			Log.printVerbose(" ---------------------------- ");
			Log.printVerbose(" dateFrom = " + dateFrom.toString());
			Log.printVerbose(" dateTo = " + dateTo.toString());
			Log.printVerbose(" nDays = " + nDays);
			// ////////////////////////////////////////////////////////////
			// start of for loop
			for (int cDay = 0; cDay < nDays; cDay++)
			{
				Timestamp tsFrom = TimeFormat.add(dateFrom, 0, 0, cDay);
				Timestamp tsTo = TimeFormat.add(tsFrom, 0, 0, 1);
				Log.printVerbose("tsFrom = " + tsFrom.toString());
				Log.printVerbose("tsTo = " + tsTo.toString());
				String strOption = "active";
				// / first, retrieve all invoices posted to nominal account
				// / on a particular day
				natForeignTable = NominalAccountTxnBean.FT_CUST_INVOICE;
				Vector vecNaInv = NominalAccountNut.getValueObjectsGiven(pcCenter, naForeignTable, naForeignKey,
						currency, natForeignTable, natForeignKey, tsFrom, tsTo, strOption);
				// / next, retrieve all receipts posted to nominal account
				// / on a particular day
				Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 2");
				natForeignTable = NominalAccountTxnBean.FT_CUST_RECEIPT;
				Vector vecNaRec = NominalAccountNut.getValueObjectsGiven(pcCenter, naForeignTable, naForeignKey,
						currency, natForeignTable, natForeignKey, tsFrom, tsTo, strOption);
				vecNaInv.addAll(vecNaRec);
				// / next find out if there's duplicate of NominalAccount in the
				// list
				// / above, if there are, merge them
				Vector vecMerged = NominalAccountNut.fnMergeNominalAccountWithSameID(vecNaInv);
				Vector vecInvRec = new Vector();
				Vector vecRecOnly = new Vector();
				Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 3");
				for (int count1 = 0; count1 < vecMerged.size(); count1++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecMerged.get(count1);
					BigDecimal bdTotalInvoice = new BigDecimal("0.00");
					BigDecimal bdTotalReceipt = new BigDecimal("0.00");
					Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 4");
					for (int count2 = 0; count2 < naObj.vecNominalAccountTxn.size(); count2++)
					{
						NominalAccountTxnObject natObj = (NominalAccountTxnObject) naObj.vecNominalAccountTxn
								.get(count2);
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_INVOICE))
						{
							Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
							bdTotalInvoice = bdTotalInvoice.add(natObj.amount);
						} else if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_RECEIPT))
						{
							Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
							bdTotalReceipt = bdTotalReceipt.add(natObj.amount);
						}
					}
					if (bdTotalInvoice.signum() == 0)
					{
						// receipts only vector
						vecRecOnly.add(naObj);
						Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
					} else
					{
						vecInvRec.add(naObj);
						Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
					}
				}
				/*
				 * for(int count1=0;count1<vecInvRec.size();count1++) {
				 * NominalAccountObject naObj = (NominalAccountObject)
				 * vecInvRec.get(count1);
				 * if(naObj.foreignTable.equals(NominalAccountBean.FT_CUSTOMER)) {
				 * try { CustAccount caEJB =
				 * CustAccountNut.getHandle(naObj.foreignKey);
				 * Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
				 * 
				 * Integer iType = caEJB.getAccType(); if(iType.intValue()==
				 * CustAccountBean.ACCTYPE_INTERNAL_ENUM.intValue()) {
				 * vecInvRecInternal.add(naObj);} else {
				 * vecInvRecExternal.add(naObj);} } catch(Exception ex) {
				 * ex.printStackTrace(); } } }
				 * 
				 */
				for (int count1 = 0; count1 < vecRecOnly.size(); count1++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecRecOnly.get(count1);
					if (naObj.foreignTable.equals(NominalAccountBean.FT_CUSTOMER))
					{
						Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
						try
						{
							CustAccount caEJB = CustAccountNut.getHandle(naObj.foreignKey);
							Integer iType = caEJB.getAccType();
							if (iType.intValue() == CustAccountBean.ACCTYPE_CORPORATE.intValue())
							{
								vecRecOnlyInternal.add(naObj);
							} else
							{
								vecRecOnlyExternal.add(naObj);
							}
						} catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
				}
			}// / end of for cDay
			// / next, split this vector into internal/external vector
			// / base on customer type
			Log.printVerbose(" after calling na nut to retrieve na txns !!");
			req.setAttribute("vecInvRecInternal", vecInvRecInternal);// empty
			req.setAttribute("vecInvRecExternal", vecInvRecExternal);// empty
			req.setAttribute("vecRecOnlyExternal", vecRecOnlyExternal);
			req.setAttribute("vecRecOnlyInternal", vecRecOnlyInternal);
			fnAuditTrail(servlet, req, res);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "mgt_report: monthly-debtors-collection-report";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}// end of DoMgtMonthlyDebtorCollectionReportType01
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.management;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.accounting.*;

public class DoMgtMonthlyDebtorCollectionReportType01 implements Action
{
	String strClassName = "DoMgtMonthlyDebtorCollectionReportType01";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		String fwdPage = (String) req.getParameter("fwdPage");
		Vector vecPCCenter = ProfitCostCenterNut.getValueObjectsGiven(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.STATUS_ACTIVE, (String) null, (String) null);
		req.setAttribute("vecPCCenter", vecPCCenter);
		fnGetReportType01Values(servlet, req, res);
		if (fwdPage != null)
		{
			return new ActionRouter(fwdPage);
		}
		return new ActionRouter("mgt-monthly-debtor-collection-report-type01-page");
	}

	public void fnGetReportType01Values(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		try
		{
			Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
			String strPCCenter = req.getParameter("pcCenter");
			req.setAttribute("strPCCenter", strPCCenter);
			String currency = req.getParameter("currency");
			req.setAttribute("currency", currency);
			String strDateFrom = req.getParameter("dateFrom");
			req.setAttribute("dateFrom", strDateFrom);
			String strDateTo = req.getParameter("dateTo");
			req.setAttribute("dateTo", strDateTo);
			if (strPCCenter == null || currency == null || strDateFrom == null)
			{
				return;
			}
			Integer pcCenter = null;
			try
			{
				pcCenter = new Integer(strPCCenter);
			} catch (Exception ex)
			{ // do nothing, when "all" is selected
			}
			// // NominalAccount ForeignTable
			// /// NOT NominalAccountTxn ForeignTable
			String naForeignTable = NominalAccountBean.FT_CUSTOMER;
			Integer naForeignKey = null;// new Integer("0");
			Long natForeignKey = null;
			String natForeignTable = null;// =
											// NominalAccountTxnBean.FT_CUST_INVOICE;
			Vector vecInvRecExternal = new Vector();
			Vector vecInvRecInternal = new Vector();
			Vector vecRecOnlyExternal = new Vector();
			Vector vecRecOnlyInternal = new Vector();
			// / count number of days
			Timestamp dateFrom = TimeFormat.createTimestamp(strDateFrom);
			Timestamp dateTo = TimeFormat.createTimestamp(strDateTo);
			Timestamp dateRun = TimeFormat.createTimestamp(strDateFrom);
			int nDays = 0;
			while (dateRun.getTime() < dateTo.getTime())
			{
				nDays += 1;
				dateRun = TimeFormat.add(dateFrom, 0, 0, nDays);
				Log.printVerbose(" dateRun = " + dateRun.toString());
				Log.printVerbose(" nDays = " + nDays);
			}
			Log.printVerbose(" ---------------------------- ");
			Log.printVerbose(" dateFrom = " + dateFrom.toString());
			Log.printVerbose(" dateTo = " + dateTo.toString());
			Log.printVerbose(" nDays = " + nDays);
			// ////////////////////////////////////////////////////////////
			// start of for loop
			for (int cDay = 0; cDay < nDays; cDay++)
			{
				Timestamp tsFrom = TimeFormat.add(dateFrom, 0, 0, cDay);
				Timestamp tsTo = TimeFormat.add(tsFrom, 0, 0, 1);
				Log.printVerbose("tsFrom = " + tsFrom.toString());
				Log.printVerbose("tsTo = " + tsTo.toString());
				String strOption = "active";
				// / first, retrieve all invoices posted to nominal account
				// / on a particular day
				natForeignTable = NominalAccountTxnBean.FT_CUST_INVOICE;
				Vector vecNaInv = NominalAccountNut.getValueObjectsGiven(pcCenter, naForeignTable, naForeignKey,
						currency, natForeignTable, natForeignKey, tsFrom, tsTo, strOption);
				// / next, retrieve all receipts posted to nominal account
				// / on a particular day
				Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 2");
				natForeignTable = NominalAccountTxnBean.FT_CUST_RECEIPT;
				Vector vecNaRec = NominalAccountNut.getValueObjectsGiven(pcCenter, naForeignTable, naForeignKey,
						currency, natForeignTable, natForeignKey, tsFrom, tsTo, strOption);
				vecNaInv.addAll(vecNaRec);
				// / next find out if there's duplicate of NominalAccount in the
				// list
				// / above, if there are, merge them
				Vector vecMerged = NominalAccountNut.fnMergeNominalAccountWithSameID(vecNaInv);
				Vector vecInvRec = new Vector();
				Vector vecRecOnly = new Vector();
				Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 3");
				for (int count1 = 0; count1 < vecMerged.size(); count1++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecMerged.get(count1);
					BigDecimal bdTotalInvoice = new BigDecimal("0.00");
					BigDecimal bdTotalReceipt = new BigDecimal("0.00");
					Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 4");
					for (int count2 = 0; count2 < naObj.vecNominalAccountTxn.size(); count2++)
					{
						NominalAccountTxnObject natObj = (NominalAccountTxnObject) naObj.vecNominalAccountTxn
								.get(count2);
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_INVOICE))
						{
							Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
							bdTotalInvoice = bdTotalInvoice.add(natObj.amount);
						} else if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_RECEIPT))
						{
							Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
							bdTotalReceipt = bdTotalReceipt.add(natObj.amount);
						}
					}
					if (bdTotalInvoice.signum() == 0)
					{
						// receipts only vector
						vecRecOnly.add(naObj);
						Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
					} else
					{
						vecInvRec.add(naObj);
						Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
					}
				}
				/*
				 * for(int count1=0;count1<vecInvRec.size();count1++) {
				 * NominalAccountObject naObj = (NominalAccountObject)
				 * vecInvRec.get(count1);
				 * if(naObj.foreignTable.equals(NominalAccountBean.FT_CUSTOMER)) {
				 * try { CustAccount caEJB =
				 * CustAccountNut.getHandle(naObj.foreignKey);
				 * Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
				 * 
				 * Integer iType = caEJB.getAccType(); if(iType.intValue()==
				 * CustAccountBean.ACCTYPE_INTERNAL_ENUM.intValue()) {
				 * vecInvRecInternal.add(naObj);} else {
				 * vecInvRecExternal.add(naObj);} } catch(Exception ex) {
				 * ex.printStackTrace(); } } }
				 * 
				 */
				for (int count1 = 0; count1 < vecRecOnly.size(); count1++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecRecOnly.get(count1);
					if (naObj.foreignTable.equals(NominalAccountBean.FT_CUSTOMER))
					{
						Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
						try
						{
							CustAccount caEJB = CustAccountNut.getHandle(naObj.foreignKey);
							Integer iType = caEJB.getAccType();
							if (iType.intValue() == CustAccountBean.ACCTYPE_CORPORATE.intValue())
							{
								vecRecOnlyInternal.add(naObj);
							} else
							{
								vecRecOnlyExternal.add(naObj);
							}
						} catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
				}
			}// / end of for cDay
			// / next, split this vector into internal/external vector
			// / base on customer type
			Log.printVerbose(" after calling na nut to retrieve na txns !!");
			req.setAttribute("vecInvRecInternal", vecInvRecInternal);// empty
			req.setAttribute("vecInvRecExternal", vecInvRecExternal);// empty
			req.setAttribute("vecRecOnlyExternal", vecRecOnlyExternal);
			req.setAttribute("vecRecOnlyInternal", vecRecOnlyInternal);
			fnAuditTrail(servlet, req, res);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "mgt_report: monthly-debtors-collection-report";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}// end of DoMgtMonthlyDebtorCollectionReportType01
