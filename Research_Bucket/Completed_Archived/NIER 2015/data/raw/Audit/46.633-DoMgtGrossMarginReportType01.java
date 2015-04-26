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

public class DoMgtGrossMarginReportType01 implements Action
{
	String strClassName = "DoMgtGrossMarginReportType01";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		String fwdPage = (String) req.getParameter("fwdPage");
		if (fwdPage == null)
		{
			fwdPage = "mgt-gross-margin-report-type01-page";
		}
		Vector vecCustSvcCtr = CustServiceCenterNut.getValueObjectsGiven(CustServiceCenterBean.STATUS,
				CustServiceCenterBean.STATUS_ACTIVE, (String) null, (String) null);
		req.setAttribute("vecCustSvcCtr", vecCustSvcCtr);
		String svcCtr = req.getParameter("svcCtr");
		req.setAttribute("svcCtr", svcCtr);
		String dateFrom = req.getParameter("dateFrom");
		req.setAttribute("dateFrom", dateFrom);
		String dateTo = req.getParameter("dateTo");
		req.setAttribute("dateTo", dateTo);
		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		if (formName.equals("getGrossMarginReport"))
		{
			fnGetReportType01Values(servlet, req, res);
			return new ActionRouter(fwdPage);
		}
		return new ActionRouter(fwdPage);
	}

	public void fnGetReportType01Values(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
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
		// if(tsTo!=null) { tsTo = TimeFormat.add(tsTo,0,0,1);}
		Timestamp tsTmp = TimeFormat.createTimestamp(dateFrom);
		int nDays = 0;
		for (nDays = 0; tsTmp.getTime() <= tsTo.getTime(); nDays++)
		{
			tsTmp = TimeFormat.add(tsFrom, 0, 0, nDays);
		}
		nDays--;
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
		for (int cnt = 0; cnt < nDays; cnt++)
		{
			Timestamp ts1 = TimeFormat.add(tsFrom, 0, 0, cnt);
			Timestamp ts2 = TimeFormat.add(tsFrom, 0, 0, cnt + 1);
			Vector vecInvoices = InvoiceNut.getValueObjects(mStmtNumber, mSalesTxnId, mPaymentTermsId, ts1, ts2,
					mCurrency, mRemarks, mState, mStatus, mUserIdUpdate, (BigDecimal) null, // outstdAmt
																							// more
																							// than
																							// or
																							// equal
					(BigDecimal) null, // outstdAmt less than or equal
					// salesTxnAttributes
					sTxnCustAccId, sTxnCustSvcCtrId, sTxnState, sTxnStatus);
			Vector tmpVecSTxn = new Vector();
			for (int cnt2 = 0; cnt2 < vecInvoices.size(); cnt2++)
			{
				InvoiceObject invObj = (InvoiceObject) vecInvoices.get(cnt2);
				SalesTxnObject sTxnObj = SalesTxnNut.getObject(invObj.mPkid);
				tmpVecSTxn.add(sTxnObj);
			}
			vecSalesTxnTree.add(tmpVecSTxn);
		}
		req.setAttribute("vecSalesTxnTree", vecSalesTxnTree);
		fnAuditTrail(servlet, req, res);
	}// end of function

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "mgt_report: gross-margin-report";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}// end of DoMgtGrossMarginReportType01
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

public class DoMgtGrossMarginReportType01 implements Action
{
	String strClassName = "DoMgtGrossMarginReportType01";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		String fwdPage = (String) req.getParameter("fwdPage");
		if (fwdPage == null)
		{
			fwdPage = "mgt-gross-margin-report-type01-page";
		}
		Vector vecCustSvcCtr = CustServiceCenterNut.getValueObjectsGiven(CustServiceCenterBean.STATUS,
				CustServiceCenterBean.STATUS_ACTIVE, (String) null, (String) null);
		req.setAttribute("vecCustSvcCtr", vecCustSvcCtr);
		String svcCtr = req.getParameter("svcCtr");
		req.setAttribute("svcCtr", svcCtr);
		String dateFrom = req.getParameter("dateFrom");
		req.setAttribute("dateFrom", dateFrom);
		String dateTo = req.getParameter("dateTo");
		req.setAttribute("dateTo", dateTo);
		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		if (formName.equals("getGrossMarginReport"))
		{
			fnGetReportType01Values(servlet, req, res);
			return new ActionRouter(fwdPage);
		}
		return new ActionRouter(fwdPage);
	}

	public void fnGetReportType01Values(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
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
		// if(tsTo!=null) { tsTo = TimeFormat.add(tsTo,0,0,1);}
		Timestamp tsTmp = TimeFormat.createTimestamp(dateFrom);
		int nDays = 0;
		for (nDays = 0; tsTmp.getTime() <= tsTo.getTime(); nDays++)
		{
			tsTmp = TimeFormat.add(tsFrom, 0, 0, nDays);
		}
		nDays--;
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
		for (int cnt = 0; cnt < nDays; cnt++)
		{
			Timestamp ts1 = TimeFormat.add(tsFrom, 0, 0, cnt);
			Timestamp ts2 = TimeFormat.add(tsFrom, 0, 0, cnt + 1);
			Vector vecInvoices = InvoiceNut.getValueObjects(mStmtNumber, mSalesTxnId, mPaymentTermsId, ts1, ts2,
					mCurrency, mRemarks, mState, mStatus, mUserIdUpdate, (BigDecimal) null, // outstdAmt
																							// more
																							// than
																							// or
																							// equal
					(BigDecimal) null, // outstdAmt less than or equal
					// salesTxnAttributes
					sTxnCustAccId, sTxnCustSvcCtrId, sTxnState, sTxnStatus);
			Vector tmpVecSTxn = new Vector();
			for (int cnt2 = 0; cnt2 < vecInvoices.size(); cnt2++)
			{
				InvoiceObject invObj = (InvoiceObject) vecInvoices.get(cnt2);
				SalesTxnObject sTxnObj = SalesTxnNut.getObject(invObj.mPkid);
				tmpVecSTxn.add(sTxnObj);
			}
			vecSalesTxnTree.add(tmpVecSTxn);
		}
		req.setAttribute("vecSalesTxnTree", vecSalesTxnTree);
		fnAuditTrail(servlet, req, res);
	}// end of function

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "mgt_report: gross-margin-report";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}// end of DoMgtGrossMarginReportType01
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

public class DoMgtGrossMarginReportType01 implements Action
{
	String strClassName = "DoMgtGrossMarginReportType01";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		String fwdPage = (String) req.getParameter("fwdPage");
		if (fwdPage == null)
		{
			fwdPage = "mgt-gross-margin-report-type01-page";
		}
		Vector vecCustSvcCtr = CustServiceCenterNut.getValueObjectsGiven(CustServiceCenterBean.STATUS,
				CustServiceCenterBean.STATUS_ACTIVE, (String) null, (String) null);
		req.setAttribute("vecCustSvcCtr", vecCustSvcCtr);
		String svcCtr = req.getParameter("svcCtr");
		req.setAttribute("svcCtr", svcCtr);
		String dateFrom = req.getParameter("dateFrom");
		req.setAttribute("dateFrom", dateFrom);
		String dateTo = req.getParameter("dateTo");
		req.setAttribute("dateTo", dateTo);
		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		if (formName.equals("getGrossMarginReport"))
		{
			fnGetReportType01Values(servlet, req, res);
			return new ActionRouter(fwdPage);
		}
		return new ActionRouter(fwdPage);
	}

	public void fnGetReportType01Values(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
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
		// if(tsTo!=null) { tsTo = TimeFormat.add(tsTo,0,0,1);}
		Timestamp tsTmp = TimeFormat.createTimestamp(dateFrom);
		int nDays = 0;
		for (nDays = 0; tsTmp.getTime() <= tsTo.getTime(); nDays++)
		{
			tsTmp = TimeFormat.add(tsFrom, 0, 0, nDays);
		}
		nDays--;
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
		for (int cnt = 0; cnt < nDays; cnt++)
		{
			Timestamp ts1 = TimeFormat.add(tsFrom, 0, 0, cnt);
			Timestamp ts2 = TimeFormat.add(tsFrom, 0, 0, cnt + 1);
			Vector vecInvoices = InvoiceNut.getValueObjects(mStmtNumber, mSalesTxnId, mPaymentTermsId, ts1, ts2,
					mCurrency, mRemarks, mState, mStatus, mUserIdUpdate, (BigDecimal) null, // outstdAmt
																							// more
																							// than
																							// or
																							// equal
					(BigDecimal) null, // outstdAmt less than or equal
					// salesTxnAttributes
					sTxnCustAccId, sTxnCustSvcCtrId, sTxnState, sTxnStatus);
			Vector tmpVecSTxn = new Vector();
			for (int cnt2 = 0; cnt2 < vecInvoices.size(); cnt2++)
			{
				InvoiceObject invObj = (InvoiceObject) vecInvoices.get(cnt2);
				SalesTxnObject sTxnObj = SalesTxnNut.getObject(invObj.mPkid);
				tmpVecSTxn.add(sTxnObj);
			}
			vecSalesTxnTree.add(tmpVecSTxn);
		}
		req.setAttribute("vecSalesTxnTree", vecSalesTxnTree);
		fnAuditTrail(servlet, req, res);
	}// end of function

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "mgt_report: gross-margin-report";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}// end of DoMgtGrossMarginReportType01
