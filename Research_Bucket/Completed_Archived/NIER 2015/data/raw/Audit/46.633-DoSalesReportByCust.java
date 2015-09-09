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

public class DoSalesReportByCust implements Action
{
	private String strClassName = "DoSalesReportByCust";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Get the formName
		String formName = req.getParameter("formName");
		String fwdPage = req.getParameter("fwdPage");
		String svcCtr = req.getParameter("svcCtr");
		req.setAttribute("svcCtr", svcCtr);
		String dateFrom = req.getParameter("dateFrom");
		req.setAttribute("dateFrom", dateFrom);
		String dateTo = req.getParameter("dateTo");
		req.setAttribute("dateTo", dateTo);
		Vector vecCustSvcCtr = CustServiceCenterNut.getValueObjectsGiven(CustServiceCenterBean.STATUS,
				CustServiceCenterBean.STATUS_ACTIVE, (String) null, (String) null);
		req.setAttribute("vecCustSvcCtr", vecCustSvcCtr);
		if (fwdPage == null)
		{
			fwdPage = "mgt-sales-report-by-cust-type01-page";
		}
		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		if (formName.equals("getSalesReportByCust"))
		{
			fnGetSalesReportByCust(servlet, req, res);
			return new ActionRouter(fwdPage);
		}
		return new ActionRouter(fwdPage);
	}

	protected void fnGetSalesReportByCust(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String svcCtr = req.getParameter("svcCtr");
		req.setAttribute("svcCtr", svcCtr);
		String dateFrom = req.getParameter("dateFrom");
		req.setAttribute("dateFrom", dateFrom);
		String dateTo = req.getParameter("dateTo");
		req.setAttribute("dateTo", dateTo);
		Timestamp tsFrom = null;
		Timestamp tsTo = null;
		try
		{
			tsFrom = TimeFormat.createTimestamp(dateFrom);
		} catch (Exception ex)
		{
		}
		try
		{
			tsTo = TimeFormat.createTimestamp(dateTo);
		} catch (Exception ex)
		{
		}
		if (tsTo != null)
		{
			tsTo = TimeFormat.add(tsTo, 0, 0, 1);
		}
		String tsType = null;
		Integer iCustAcc = null;
		Integer iSvcCtrId = null;
		String strState = null;
		String strStatus = "active";
		Integer iUsrEdit = null;
		Long mStmtNumber = null;
		Long mSalesTxnId = null;
		Integer mPaymentTermsId = null;
		String mCurrency = null;
		String mRemarks = null;
		String mState = "posted";
		String mStatus = "active";
		Integer mUserIdUpdate = null;
		try
		{
			iSvcCtrId = new Integer(svcCtr);
		} catch (Exception ex)
		{ /* do nothing- all svcCtr */
		}
		Integer sTxnCustAccId = null;
		Integer sTxnCustSvcCtrId = iSvcCtrId;
		String sTxnState = null;
		String sTxnStatus = null;
		Vector vecSalesTxnTree = new Vector();
		Vector vecCustAcc = new Vector();
		Vector vecInvoices = InvoiceNut.getValueObjects(mStmtNumber, mSalesTxnId, mPaymentTermsId, tsFrom, tsTo,
				mCurrency, mRemarks, mState, mStatus, mUserIdUpdate, (BigDecimal) null, // outstdAmt
																						// more
																						// than
																						// or
																						// equal
				(BigDecimal) null, // outstdAmt less than or equal
				// salesTxnAttributes
				sTxnCustAccId, sTxnCustSvcCtrId, sTxnState, sTxnStatus);
		for (int count1 = 0; count1 < vecInvoices.size(); count1++)
		{
			Log.printVerbose(" one thousand " + count1);
			InvoiceObject invObj = (InvoiceObject) vecInvoices.get(count1);
			SalesTxnObject stObjTree = SalesTxnNut.getObject(invObj.mSalesTxnId);
			boolean bMatch = false;
			int index = vecCustAcc.size() + 1;
			for (int count2 = 0; count2 < vecCustAcc.size(); count2++)
			{
				Integer iCustIdNew = stObjTree.custAccId;
				Vector tmpSalesTxnTree = (Vector) vecCustAcc.get(count2);
				SalesTxnObject firstStxnObj = (SalesTxnObject) tmpSalesTxnTree.get(0);
				Integer theCustId = firstStxnObj.custAccId;
				if (theCustId.equals(iCustIdNew))
				{
					bMatch = true;
					index = count2;
					break;
				}
			}
			if (bMatch)// true
			{
				// when there's a match, append to existing vector
				// group the sales txn obj with same custId together
				Vector tmpVecSalesTxn = (Vector) vecCustAcc.get(index);
				tmpVecSalesTxn.add(stObjTree);
				vecCustAcc.set(index, tmpVecSalesTxn);
			} else
			{
				// when there's no match, create a new vector
				Vector tmpVecSalesTxn = new Vector();
				tmpVecSalesTxn.add(stObjTree);
				vecCustAcc.add(tmpVecSalesTxn);
			}
			vecSalesTxnTree.add(stObjTree);
		}
		req.setAttribute("vecCustAcc", vecCustAcc);
		req.setAttribute("vecSalesTxnTree", vecSalesTxnTree);
		fnAuditTrail(servlet, req, res);
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
			atObj.remarks = "mgt_report: sales-report-by-customer";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}// end of DoSalesReportByCust
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

public class DoSalesReportByCust implements Action
{
	private String strClassName = "DoSalesReportByCust";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Get the formName
		String formName = req.getParameter("formName");
		String fwdPage = req.getParameter("fwdPage");
		String svcCtr = req.getParameter("svcCtr");
		req.setAttribute("svcCtr", svcCtr);
		String dateFrom = req.getParameter("dateFrom");
		req.setAttribute("dateFrom", dateFrom);
		String dateTo = req.getParameter("dateTo");
		req.setAttribute("dateTo", dateTo);
		Vector vecCustSvcCtr = CustServiceCenterNut.getValueObjectsGiven(CustServiceCenterBean.STATUS,
				CustServiceCenterBean.STATUS_ACTIVE, (String) null, (String) null);
		req.setAttribute("vecCustSvcCtr", vecCustSvcCtr);
		if (fwdPage == null)
		{
			fwdPage = "mgt-sales-report-by-cust-type01-page";
		}
		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		if (formName.equals("getSalesReportByCust"))
		{
			fnGetSalesReportByCust(servlet, req, res);
			return new ActionRouter(fwdPage);
		}
		return new ActionRouter(fwdPage);
	}

	protected void fnGetSalesReportByCust(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String svcCtr = req.getParameter("svcCtr");
		req.setAttribute("svcCtr", svcCtr);
		String dateFrom = req.getParameter("dateFrom");
		req.setAttribute("dateFrom", dateFrom);
		String dateTo = req.getParameter("dateTo");
		req.setAttribute("dateTo", dateTo);
		Timestamp tsFrom = null;
		Timestamp tsTo = null;
		try
		{
			tsFrom = TimeFormat.createTimestamp(dateFrom);
		} catch (Exception ex)
		{
		}
		try
		{
			tsTo = TimeFormat.createTimestamp(dateTo);
		} catch (Exception ex)
		{
		}
		if (tsTo != null)
		{
			tsTo = TimeFormat.add(tsTo, 0, 0, 1);
		}
		String tsType = null;
		Integer iCustAcc = null;
		Integer iSvcCtrId = null;
		String strState = null;
		String strStatus = "active";
		Integer iUsrEdit = null;
		Long mStmtNumber = null;
		Long mSalesTxnId = null;
		Integer mPaymentTermsId = null;
		String mCurrency = null;
		String mRemarks = null;
		String mState = "posted";
		String mStatus = "active";
		Integer mUserIdUpdate = null;
		try
		{
			iSvcCtrId = new Integer(svcCtr);
		} catch (Exception ex)
		{ /* do nothing- all svcCtr */
		}
		Integer sTxnCustAccId = null;
		Integer sTxnCustSvcCtrId = iSvcCtrId;
		String sTxnState = null;
		String sTxnStatus = null;
		Vector vecSalesTxnTree = new Vector();
		Vector vecCustAcc = new Vector();
		Vector vecInvoices = InvoiceNut.getValueObjects(mStmtNumber, mSalesTxnId, mPaymentTermsId, tsFrom, tsTo,
				mCurrency, mRemarks, mState, mStatus, mUserIdUpdate, (BigDecimal) null, // outstdAmt
																						// more
																						// than
																						// or
																						// equal
				(BigDecimal) null, // outstdAmt less than or equal
				// salesTxnAttributes
				sTxnCustAccId, sTxnCustSvcCtrId, sTxnState, sTxnStatus);
		for (int count1 = 0; count1 < vecInvoices.size(); count1++)
		{
			Log.printVerbose(" one thousand " + count1);
			InvoiceObject invObj = (InvoiceObject) vecInvoices.get(count1);
			SalesTxnObject stObjTree = SalesTxnNut.getObject(invObj.mSalesTxnId);
			boolean bMatch = false;
			int index = vecCustAcc.size() + 1;
			for (int count2 = 0; count2 < vecCustAcc.size(); count2++)
			{
				Integer iCustIdNew = stObjTree.custAccId;
				Vector tmpSalesTxnTree = (Vector) vecCustAcc.get(count2);
				SalesTxnObject firstStxnObj = (SalesTxnObject) tmpSalesTxnTree.get(0);
				Integer theCustId = firstStxnObj.custAccId;
				if (theCustId.equals(iCustIdNew))
				{
					bMatch = true;
					index = count2;
					break;
				}
			}
			if (bMatch)// true
			{
				// when there's a match, append to existing vector
				// group the sales txn obj with same custId together
				Vector tmpVecSalesTxn = (Vector) vecCustAcc.get(index);
				tmpVecSalesTxn.add(stObjTree);
				vecCustAcc.set(index, tmpVecSalesTxn);
			} else
			{
				// when there's no match, create a new vector
				Vector tmpVecSalesTxn = new Vector();
				tmpVecSalesTxn.add(stObjTree);
				vecCustAcc.add(tmpVecSalesTxn);
			}
			vecSalesTxnTree.add(stObjTree);
		}
		req.setAttribute("vecCustAcc", vecCustAcc);
		req.setAttribute("vecSalesTxnTree", vecSalesTxnTree);
		fnAuditTrail(servlet, req, res);
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
			atObj.remarks = "mgt_report: sales-report-by-customer";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}// end of DoSalesReportByCust
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

public class DoSalesReportByCust implements Action
{
	private String strClassName = "DoSalesReportByCust";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Get the formName
		String formName = req.getParameter("formName");
		String fwdPage = req.getParameter("fwdPage");
		String svcCtr = req.getParameter("svcCtr");
		req.setAttribute("svcCtr", svcCtr);
		String dateFrom = req.getParameter("dateFrom");
		req.setAttribute("dateFrom", dateFrom);
		String dateTo = req.getParameter("dateTo");
		req.setAttribute("dateTo", dateTo);
		Vector vecCustSvcCtr = CustServiceCenterNut.getValueObjectsGiven(CustServiceCenterBean.STATUS,
				CustServiceCenterBean.STATUS_ACTIVE, (String) null, (String) null);
		req.setAttribute("vecCustSvcCtr", vecCustSvcCtr);
		if (fwdPage == null)
		{
			fwdPage = "mgt-sales-report-by-cust-type01-page";
		}
		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		if (formName.equals("getSalesReportByCust"))
		{
			fnGetSalesReportByCust(servlet, req, res);
			return new ActionRouter(fwdPage);
		}
		return new ActionRouter(fwdPage);
	}

	protected void fnGetSalesReportByCust(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String svcCtr = req.getParameter("svcCtr");
		req.setAttribute("svcCtr", svcCtr);
		String dateFrom = req.getParameter("dateFrom");
		req.setAttribute("dateFrom", dateFrom);
		String dateTo = req.getParameter("dateTo");
		req.setAttribute("dateTo", dateTo);
		Timestamp tsFrom = null;
		Timestamp tsTo = null;
		try
		{
			tsFrom = TimeFormat.createTimestamp(dateFrom);
		} catch (Exception ex)
		{
		}
		try
		{
			tsTo = TimeFormat.createTimestamp(dateTo);
		} catch (Exception ex)
		{
		}
		if (tsTo != null)
		{
			tsTo = TimeFormat.add(tsTo, 0, 0, 1);
		}
		String tsType = null;
		Integer iCustAcc = null;
		Integer iSvcCtrId = null;
		String strState = null;
		String strStatus = "active";
		Integer iUsrEdit = null;
		Long mStmtNumber = null;
		Long mSalesTxnId = null;
		Integer mPaymentTermsId = null;
		String mCurrency = null;
		String mRemarks = null;
		String mState = "posted";
		String mStatus = "active";
		Integer mUserIdUpdate = null;
		try
		{
			iSvcCtrId = new Integer(svcCtr);
		} catch (Exception ex)
		{ /* do nothing- all svcCtr */
		}
		Integer sTxnCustAccId = null;
		Integer sTxnCustSvcCtrId = iSvcCtrId;
		String sTxnState = null;
		String sTxnStatus = null;
		Vector vecSalesTxnTree = new Vector();
		Vector vecCustAcc = new Vector();
		Vector vecInvoices = InvoiceNut.getValueObjects(mStmtNumber, mSalesTxnId, mPaymentTermsId, tsFrom, tsTo,
				mCurrency, mRemarks, mState, mStatus, mUserIdUpdate, (BigDecimal) null, // outstdAmt
																						// more
																						// than
																						// or
																						// equal
				(BigDecimal) null, // outstdAmt less than or equal
				// salesTxnAttributes
				sTxnCustAccId, sTxnCustSvcCtrId, sTxnState, sTxnStatus);
		for (int count1 = 0; count1 < vecInvoices.size(); count1++)
		{
			Log.printVerbose(" one thousand " + count1);
			InvoiceObject invObj = (InvoiceObject) vecInvoices.get(count1);
			SalesTxnObject stObjTree = SalesTxnNut.getObject(invObj.mSalesTxnId);
			boolean bMatch = false;
			int index = vecCustAcc.size() + 1;
			for (int count2 = 0; count2 < vecCustAcc.size(); count2++)
			{
				Integer iCustIdNew = stObjTree.custAccId;
				Vector tmpSalesTxnTree = (Vector) vecCustAcc.get(count2);
				SalesTxnObject firstStxnObj = (SalesTxnObject) tmpSalesTxnTree.get(0);
				Integer theCustId = firstStxnObj.custAccId;
				if (theCustId.equals(iCustIdNew))
				{
					bMatch = true;
					index = count2;
					break;
				}
			}
			if (bMatch)// true
			{
				// when there's a match, append to existing vector
				// group the sales txn obj with same custId together
				Vector tmpVecSalesTxn = (Vector) vecCustAcc.get(index);
				tmpVecSalesTxn.add(stObjTree);
				vecCustAcc.set(index, tmpVecSalesTxn);
			} else
			{
				// when there's no match, create a new vector
				Vector tmpVecSalesTxn = new Vector();
				tmpVecSalesTxn.add(stObjTree);
				vecCustAcc.add(tmpVecSalesTxn);
			}
			vecSalesTxnTree.add(stObjTree);
		}
		req.setAttribute("vecCustAcc", vecCustAcc);
		req.setAttribute("vecSalesTxnTree", vecSalesTxnTree);
		fnAuditTrail(servlet, req, res);
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
			atObj.remarks = "mgt_report: sales-report-by-customer";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}// end of DoSalesReportByCust
