/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.pos;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;

public class DoMonitorInvoices implements Action
{
	String strClassName = "DoMonitorInvoices";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		Vector vecSalesTxnObj = new Vector();
		String mErrMsg = new String("");
		fnGetCustSalesCenterList(servlet, req, res);
		// FORM HANDLERS - NULL
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("pos-monitor-invoices-page");
		}
		/***********************************************************************
		 * ALEX: Routine to cancel SalesTxn, if any
		 **********************************************************************/
		String strCancelSalesPkid = req.getParameter("cancelSalesPkid");
		Long lCancelSalesPkid = null;
		if (strCancelSalesPkid != null)
		{
			try
			{
				lCancelSalesPkid = new Long(strCancelSalesPkid);
				fnCancelSalesTxn(servlet, req, res, lCancelSalesPkid);
			} catch (NumberFormatException ex)
			{
				Log.printDebug("Error while converting strCancelSalesPkid");
			}
		}
		// FORM HANDLERS - monitor invoices
		if (formName.compareTo("monitorOpenInv") == 0)
		{
			fnGetInvoices(servlet, req, res);
			return new ActionRouter("pos-monitor-invoices-page");
		} // end formName=monitorOpenInv
		return new ActionRouter("pos-monitor-invoices-page");
	}

	protected void fnGetInvoices(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetInvoices()";
		// Get the input params
		String strFromDate = req.getParameter("fromDate");
		String strToDate = req.getParameter("toDate");
		String strCustAccId = req.getParameter("custAccId");
		String strCustSvcCtrId = req.getParameter("custSvcCtrId");
		Timestamp tsFrom = null;
		Timestamp tsTo = null;
		Integer iCustAccId = null;
		Integer iCustSvcCtrId = null;
		if (strFromDate != null && !strFromDate.equals(""))
		{
			try
			{
				tsFrom = TimeFormat.createTimeStamp(strFromDate);
				req.setAttribute("tsFrom", tsFrom);
				Log.printVerbose("tsFrom = " + tsFrom.toString());
			} catch (Exception ex)
			{
				// Leave it
			}
		}
		if (strToDate != null && !strToDate.equals(""))
		{
			try
			{
				tsTo = TimeFormat.createTimeStamp(strToDate.trim());
				req.setAttribute("tsTo", tsTo);
				Log.printVerbose("tsTo (before conversion) = " + tsTo.toString());
				// need to get the next day
				tsTo = TimeFormat.add(tsTo, 0, 0, 1);
				Log.printVerbose("tsTo (after conversion) = " + tsTo.toString());
			} catch (Exception ex)
			{
				// Leave it
			}
		}
		if (strCustAccId != null)
		{
			try
			{
				iCustAccId = new Integer(strCustAccId);
				req.setAttribute("iCustAccId", iCustAccId);
				Log.printVerbose("iCustAccId = " + iCustAccId.toString());
			} catch (Exception ex)
			{
				// Leave it
			}
		}
		if (strCustSvcCtrId != null)
		{
			try
			{
				iCustSvcCtrId = new Integer(strCustSvcCtrId);
				req.setAttribute("iCustSvcCtrId", iCustSvcCtrId);
				Log.printVerbose("iCustSvcCtrId = " + iCustSvcCtrId.toString());
			} catch (Exception ex)
			{
				// Leave it
			}
		}
		Vector vecObj = InvoiceNut.monitorOpenInv(tsFrom, tsTo, iCustAccId, iCustSvcCtrId);
		if (vecObj == null)
		{
			Log.printDebug("NULL vector returned by InvoiceNut.monitorOpenInv()");
		}
		req.setAttribute("vecInvObj", vecObj);
	}

	protected void fnGetCustSalesCenterList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		// Alex: Optimizing retrieval of CustSvcCtr list
		Collection colActiveSvcCtrObj = CustServiceCenterNut.getActiveValObj();
		Iterator itrActiveSvcC = colActiveSvcCtrObj.iterator();
		req.setAttribute("itrActiveSvcC", itrActiveSvcC);
	}

	protected void fnCancelSalesTxn(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			Long salesTxnPkid)
	{
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (lUsr == null)
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
		Integer usrid = null;
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printDebug("User does not exist: " + ex.getMessage());
		}
		fnAuditTrail(servlet, req, res, salesTxnPkid);
		// SalesTxnNut.cancelTxn(salesTxnPkid, usrid);
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res, Long salesTxnPkid)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		SalesTxnObject stObj = SalesTxnNut.getObject(salesTxnPkid);
		if (iUserId != null && stObj != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.foreignTable1 = SalesTxnBean.TABLENAME;
			atObj.foreignKey1 = stObj.pkid;
			atObj.auditType = AuditTrailBean.TYPE_TXN;
			atObj.remarks = "pos: stxn-cancel ";
			atObj.remarks += ", custID=" + stObj.custAccId.toString() + " ";
			for (int cnt1 = 0; cnt1 < stObj.vecJobsheets.size(); cnt1++)
			{
				JobsheetObject jsObj = (JobsheetObject) stObj.vecJobsheets.get(cnt1);
				if (cnt1 == 0)
				{
					atObj.remarks += ", jsId=";
				} else
				{
					atObj.remarks += ",";
				}
				atObj.remarks += jsObj.mPkid.toString() + " ";
			}
			for (int cnt1 = 0; cnt1 < stObj.vecDOs.size(); cnt1++)
			{
				DeliveryOrderObject doObj = (DeliveryOrderObject) stObj.vecDOs.get(cnt1);
				if (cnt1 == 0)
				{
					atObj.remarks += ", doId=";
				} else
				{
					atObj.remarks += ",";
				}
				atObj.remarks += doObj.mPkid.toString() + " ";
			}
			for (int cnt1 = 0; cnt1 < stObj.vecInvoices.size(); cnt1++)
			{
				InvoiceObject invObj = (InvoiceObject) stObj.vecInvoices.get(cnt1);
				if (cnt1 == 0)
				{
					atObj.remarks += ", invId=";
				} else
				{
					atObj.remarks += ",";
				}
				atObj.remarks += invObj.mPkid.toString() + " ";
			}
			AuditTrailNut.fnCreate(atObj);
		}
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.pos;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;

public class DoMonitorInvoices implements Action
{
	String strClassName = "DoMonitorInvoices";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		Vector vecSalesTxnObj = new Vector();
		String mErrMsg = new String("");
		fnGetCustSalesCenterList(servlet, req, res);
		// FORM HANDLERS - NULL
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("pos-monitor-invoices-page");
		}
		/***********************************************************************
		 * ALEX: Routine to cancel SalesTxn, if any
		 **********************************************************************/
		String strCancelSalesPkid = req.getParameter("cancelSalesPkid");
		Long lCancelSalesPkid = null;
		if (strCancelSalesPkid != null)
		{
			try
			{
				lCancelSalesPkid = new Long(strCancelSalesPkid);
				fnCancelSalesTxn(servlet, req, res, lCancelSalesPkid);
			} catch (NumberFormatException ex)
			{
				Log.printDebug("Error while converting strCancelSalesPkid");
			}
		}
		// FORM HANDLERS - monitor invoices
		if (formName.compareTo("monitorOpenInv") == 0)
		{
			fnGetInvoices(servlet, req, res);
			return new ActionRouter("pos-monitor-invoices-page");
		} // end formName=monitorOpenInv
		return new ActionRouter("pos-monitor-invoices-page");
	}

	protected void fnGetInvoices(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetInvoices()";
		// Get the input params
		String strFromDate = req.getParameter("fromDate");
		String strToDate = req.getParameter("toDate");
		String strCustAccId = req.getParameter("custAccId");
		String strCustSvcCtrId = req.getParameter("custSvcCtrId");
		Timestamp tsFrom = null;
		Timestamp tsTo = null;
		Integer iCustAccId = null;
		Integer iCustSvcCtrId = null;
		if (strFromDate != null && !strFromDate.equals(""))
		{
			try
			{
				tsFrom = TimeFormat.createTimeStamp(strFromDate);
				req.setAttribute("tsFrom", tsFrom);
				Log.printVerbose("tsFrom = " + tsFrom.toString());
			} catch (Exception ex)
			{
				// Leave it
			}
		}
		if (strToDate != null && !strToDate.equals(""))
		{
			try
			{
				tsTo = TimeFormat.createTimeStamp(strToDate.trim());
				req.setAttribute("tsTo", tsTo);
				Log.printVerbose("tsTo (before conversion) = " + tsTo.toString());
				// need to get the next day
				tsTo = TimeFormat.add(tsTo, 0, 0, 1);
				Log.printVerbose("tsTo (after conversion) = " + tsTo.toString());
			} catch (Exception ex)
			{
				// Leave it
			}
		}
		if (strCustAccId != null)
		{
			try
			{
				iCustAccId = new Integer(strCustAccId);
				req.setAttribute("iCustAccId", iCustAccId);
				Log.printVerbose("iCustAccId = " + iCustAccId.toString());
			} catch (Exception ex)
			{
				// Leave it
			}
		}
		if (strCustSvcCtrId != null)
		{
			try
			{
				iCustSvcCtrId = new Integer(strCustSvcCtrId);
				req.setAttribute("iCustSvcCtrId", iCustSvcCtrId);
				Log.printVerbose("iCustSvcCtrId = " + iCustSvcCtrId.toString());
			} catch (Exception ex)
			{
				// Leave it
			}
		}
		Vector vecObj = InvoiceNut.monitorOpenInv(tsFrom, tsTo, iCustAccId, iCustSvcCtrId);
		if (vecObj == null)
		{
			Log.printDebug("NULL vector returned by InvoiceNut.monitorOpenInv()");
		}
		req.setAttribute("vecInvObj", vecObj);
	}

	protected void fnGetCustSalesCenterList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		// Alex: Optimizing retrieval of CustSvcCtr list
		Collection colActiveSvcCtrObj = CustServiceCenterNut.getActiveValObj();
		Iterator itrActiveSvcC = colActiveSvcCtrObj.iterator();
		req.setAttribute("itrActiveSvcC", itrActiveSvcC);
	}

	protected void fnCancelSalesTxn(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			Long salesTxnPkid)
	{
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (lUsr == null)
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
		Integer usrid = null;
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printDebug("User does not exist: " + ex.getMessage());
		}
		fnAuditTrail(servlet, req, res, salesTxnPkid);
		// SalesTxnNut.cancelTxn(salesTxnPkid, usrid);
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res, Long salesTxnPkid)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		SalesTxnObject stObj = SalesTxnNut.getObject(salesTxnPkid);
		if (iUserId != null && stObj != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.foreignTable1 = SalesTxnBean.TABLENAME;
			atObj.foreignKey1 = stObj.pkid;
			atObj.auditType = AuditTrailBean.TYPE_TXN;
			atObj.remarks = "pos: stxn-cancel ";
			atObj.remarks += ", custID=" + stObj.custAccId.toString() + " ";
			for (int cnt1 = 0; cnt1 < stObj.vecJobsheets.size(); cnt1++)
			{
				JobsheetObject jsObj = (JobsheetObject) stObj.vecJobsheets.get(cnt1);
				if (cnt1 == 0)
				{
					atObj.remarks += ", jsId=";
				} else
				{
					atObj.remarks += ",";
				}
				atObj.remarks += jsObj.mPkid.toString() + " ";
			}
			for (int cnt1 = 0; cnt1 < stObj.vecDOs.size(); cnt1++)
			{
				DeliveryOrderObject doObj = (DeliveryOrderObject) stObj.vecDOs.get(cnt1);
				if (cnt1 == 0)
				{
					atObj.remarks += ", doId=";
				} else
				{
					atObj.remarks += ",";
				}
				atObj.remarks += doObj.mPkid.toString() + " ";
			}
			for (int cnt1 = 0; cnt1 < stObj.vecInvoices.size(); cnt1++)
			{
				InvoiceObject invObj = (InvoiceObject) stObj.vecInvoices.get(cnt1);
				if (cnt1 == 0)
				{
					atObj.remarks += ", invId=";
				} else
				{
					atObj.remarks += ",";
				}
				atObj.remarks += invObj.mPkid.toString() + " ";
			}
			AuditTrailNut.fnCreate(atObj);
		}
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.pos;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;

public class DoMonitorInvoices implements Action
{
	String strClassName = "DoMonitorInvoices";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		Vector vecSalesTxnObj = new Vector();
		String mErrMsg = new String("");
		fnGetCustSalesCenterList(servlet, req, res);
		// FORM HANDLERS - NULL
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("pos-monitor-invoices-page");
		}
		/***********************************************************************
		 * ALEX: Routine to cancel SalesTxn, if any
		 **********************************************************************/
		String strCancelSalesPkid = req.getParameter("cancelSalesPkid");
		Long lCancelSalesPkid = null;
		if (strCancelSalesPkid != null)
		{
			try
			{
				lCancelSalesPkid = new Long(strCancelSalesPkid);
				fnCancelSalesTxn(servlet, req, res, lCancelSalesPkid);
			} catch (NumberFormatException ex)
			{
				Log.printDebug("Error while converting strCancelSalesPkid");
			}
		}
		// FORM HANDLERS - monitor invoices
		if (formName.compareTo("monitorOpenInv") == 0)
		{
			fnGetInvoices(servlet, req, res);
			return new ActionRouter("pos-monitor-invoices-page");
		} // end formName=monitorOpenInv
		return new ActionRouter("pos-monitor-invoices-page");
	}

	protected void fnGetInvoices(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetInvoices()";
		// Get the input params
		String strFromDate = req.getParameter("fromDate");
		String strToDate = req.getParameter("toDate");
		String strCustAccId = req.getParameter("custAccId");
		String strCustSvcCtrId = req.getParameter("custSvcCtrId");
		Timestamp tsFrom = null;
		Timestamp tsTo = null;
		Integer iCustAccId = null;
		Integer iCustSvcCtrId = null;
		if (strFromDate != null && !strFromDate.equals(""))
		{
			try
			{
				tsFrom = TimeFormat.createTimeStamp(strFromDate);
				req.setAttribute("tsFrom", tsFrom);
				Log.printVerbose("tsFrom = " + tsFrom.toString());
			} catch (Exception ex)
			{
				// Leave it
			}
		}
		if (strToDate != null && !strToDate.equals(""))
		{
			try
			{
				tsTo = TimeFormat.createTimeStamp(strToDate.trim());
				req.setAttribute("tsTo", tsTo);
				Log.printVerbose("tsTo (before conversion) = " + tsTo.toString());
				// need to get the next day
				tsTo = TimeFormat.add(tsTo, 0, 0, 1);
				Log.printVerbose("tsTo (after conversion) = " + tsTo.toString());
			} catch (Exception ex)
			{
				// Leave it
			}
		}
		if (strCustAccId != null)
		{
			try
			{
				iCustAccId = new Integer(strCustAccId);
				req.setAttribute("iCustAccId", iCustAccId);
				Log.printVerbose("iCustAccId = " + iCustAccId.toString());
			} catch (Exception ex)
			{
				// Leave it
			}
		}
		if (strCustSvcCtrId != null)
		{
			try
			{
				iCustSvcCtrId = new Integer(strCustSvcCtrId);
				req.setAttribute("iCustSvcCtrId", iCustSvcCtrId);
				Log.printVerbose("iCustSvcCtrId = " + iCustSvcCtrId.toString());
			} catch (Exception ex)
			{
				// Leave it
			}
		}
		Vector vecObj = InvoiceNut.monitorOpenInv(tsFrom, tsTo, iCustAccId, iCustSvcCtrId);
		if (vecObj == null)
		{
			Log.printDebug("NULL vector returned by InvoiceNut.monitorOpenInv()");
		}
		req.setAttribute("vecInvObj", vecObj);
	}

	protected void fnGetCustSalesCenterList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		// Alex: Optimizing retrieval of CustSvcCtr list
		Collection colActiveSvcCtrObj = CustServiceCenterNut.getActiveValObj();
		Iterator itrActiveSvcC = colActiveSvcCtrObj.iterator();
		req.setAttribute("itrActiveSvcC", itrActiveSvcC);
	}

	protected void fnCancelSalesTxn(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			Long salesTxnPkid)
	{
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (lUsr == null)
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
		Integer usrid = null;
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printDebug("User does not exist: " + ex.getMessage());
		}
		fnAuditTrail(servlet, req, res, salesTxnPkid);
		// SalesTxnNut.cancelTxn(salesTxnPkid, usrid);
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res, Long salesTxnPkid)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		SalesTxnObject stObj = SalesTxnNut.getObject(salesTxnPkid);
		if (iUserId != null && stObj != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.foreignTable1 = SalesTxnBean.TABLENAME;
			atObj.foreignKey1 = stObj.pkid;
			atObj.auditType = AuditTrailBean.TYPE_TXN;
			atObj.remarks = "pos: stxn-cancel ";
			atObj.remarks += ", custID=" + stObj.custAccId.toString() + " ";
			for (int cnt1 = 0; cnt1 < stObj.vecJobsheets.size(); cnt1++)
			{
				JobsheetObject jsObj = (JobsheetObject) stObj.vecJobsheets.get(cnt1);
				if (cnt1 == 0)
				{
					atObj.remarks += ", jsId=";
				} else
				{
					atObj.remarks += ",";
				}
				atObj.remarks += jsObj.mPkid.toString() + " ";
			}
			for (int cnt1 = 0; cnt1 < stObj.vecDOs.size(); cnt1++)
			{
				DeliveryOrderObject doObj = (DeliveryOrderObject) stObj.vecDOs.get(cnt1);
				if (cnt1 == 0)
				{
					atObj.remarks += ", doId=";
				} else
				{
					atObj.remarks += ",";
				}
				atObj.remarks += doObj.mPkid.toString() + " ";
			}
			for (int cnt1 = 0; cnt1 < stObj.vecInvoices.size(); cnt1++)
			{
				InvoiceObject invObj = (InvoiceObject) stObj.vecInvoices.get(cnt1);
				if (cnt1 == 0)
				{
					atObj.remarks += ", invId=";
				} else
				{
					atObj.remarks += ",";
				}
				atObj.remarks += invObj.mPkid.toString() + " ";
			}
			AuditTrailNut.fnCreate(atObj);
		}
	}
}
