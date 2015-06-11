/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.procurement;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoEditInvoiceFromSupplier implements Action
{
	String strClassName = "DoEditInvoiceFromSupplier";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		String strKeyword = req.getParameter("strKeyword");
		if (strKeyword != null)
		{
			req.setAttribute("strKeyword", strKeyword);
		}
		// If the form name is null,
		if (formName == null)
		{
			return new ActionRouter("supp-invoice-edit-1-page");
		}
		if (formName.equals("searchSuppInvoice"))
		{
			fnSearchSuppInvoice(servlet, req, res);
			return new ActionRouter("supp-invoice-edit-1-page");
		}
		// If the form has been filled and user submit to create
		// a new purchaseOrder, do the necessary checking
		if (formName.equals("selectSuppInvoice"))
		{
			String strSiPkid = req.getParameter("siPkid");
			req.setAttribute("siPkid", strSiPkid);
			Long siPkid = new Long(strSiPkid);
			SuppInvoiceObject siObj = SuppInvoiceNut.getObject(siPkid);
			req.setAttribute("siObj", siObj);
			return new ActionRouter("supp-invoice-edit-2-page");
		}
		if (formName.equals("editSuppInvoice"))
		{
			fnEditSuppInvoice(servlet, req, res);
			String strSiPkid = req.getParameter("siPkid");
			req.setAttribute("siPkid", strSiPkid);
			Long siPkid = new Long(strSiPkid);
			SuppInvoiceObject siObj = SuppInvoiceNut.getObject(siPkid);
			req.setAttribute("siObj", siObj);
			return new ActionRouter("supp-invoice-edit-2-page");
		}
		if (formName.equals("cancelSuppInvoice"))
		{
			fnCancelSuppInvoice(servlet, req, res);
			fnAuditTrail(servlet, req, res);
			return new ActionRouter("supp-invoice-edit-1-page");
		}
		Log.printVerbose(strClassName + ": returning default ActionRouter");
		return new ActionRouter("supp-invoice-edit-1-page");
	}

	// /////////////////////////////////////////////////////////////////
	// //
	protected void fnEditSuppInvoice(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strSiPkid = req.getParameter("siPkid");
		String invoiceNumber = req.getParameter("invoiceNumber");
		String amountDiscount = req.getParameter("amountDiscount");
		String amountNet = req.getParameter("amountNet");
		String dateReceive = req.getParameter("dateReceive");
		String dateInvoice = req.getParameter("dateInvoice");
		String dateDue = req.getParameter("dateDue");
		try
		{
			BigDecimal bdAmountDiscount = new BigDecimal(amountDiscount);
			BigDecimal bdAmountNet = new BigDecimal(amountNet);
			Timestamp tsDateReceive = TimeFormat.createTimestamp(dateReceive);
			Timestamp tsDateInvoice = TimeFormat.createTimestamp(dateInvoice);
			Timestamp tsDateDue = TimeFormat.createTimestamp(dateDue);
			Long siPkid = new Long(strSiPkid);
			SuppInvoice siEJB = SuppInvoiceNut.getHandle(siPkid);
			siEJB.setInvoiceNumber(invoiceNumber);
			siEJB.setAmountDiscount(bdAmountDiscount);
			siEJB.setAmountNet(bdAmountNet);
			siEJB.setDateReceive(tsDateReceive);
			siEJB.setDateInvoice(tsDateInvoice);
			siEJB.setDateDue(tsDateDue);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			return;
		}
	}

	// /////////////////////////////////////////////////////////////////
	// //
	protected void fnCancelSuppInvoice(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strSiPkid = req.getParameter("siPkid");
		Long siPkid = new Long(strSiPkid);
		SuppInvoice siEJB = SuppInvoiceNut.getHandle(siPkid);
		if (siEJB == null)
		{
			String errMsg = " The SuppInvoice " + strSiPkid + " does not exist ";
			req.setAttribute("errMsg", errMsg);
			return;
		}
		try
		{
			String status = siEJB.getStatus();
			if (!status.equals(SuppInvoiceBean.STATUS_ACTIVE))
			{
				String errMsg = " You can cancel active only." + " Supp Invoice " + strSiPkid + " is not active ";
				req.setAttribute("errMsg", errMsg);
			}
			String state = siEJB.getState();
			if (state.equals(SuppInvoiceBean.ST_POSTED))
			{
				Vector vecNatObj = NominalAccountTxnNut.getValueObjectsGiven(NominalAccountTxnBean.FOREIGN_TABLE,
						NominalAccountTxnBean.FT_SUPP_INVOICE, NominalAccountTxnBean.FOREIGN_KEY, strSiPkid);
				for (int count = 0; count < vecNatObj.size(); count++)
				{
					NominalAccountTxnObject natObj = (NominalAccountTxnObject) vecNatObj.get(count);
					NominalAccountTxn natEJB = NominalAccountTxnNut.getHandle(natObj.pkid);
					NominalAccount naEJB = NominalAccountNut.getHandle(natEJB.getNominalAccount());
					// BigDecimal bdBalance = naEJB.getAmount();
					// bdBalance = bdBalance.add(siEJB.getAmountNet());
					// naEJB.setAmount(bdBalance);
					naEJB.addAmount(naEJB.getAmount());
					natEJB.setStatus(NominalAccountTxnBean.STATUS_CANCELLED);
				} // end for
			} // end if
			siEJB.setStatus(SuppInvoiceBean.STATUS_CANCELLED);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	// /////////////////////////////////////////////////////////////////
	// //
	protected void fnSearchSuppInvoice(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strKeyword = (String) req.getParameter("strKeyword");
		Vector vecSiObj = SuppInvoiceNut.getValueObjectsILike(strKeyword);
		req.setAttribute("vecSiObj", vecSiObj);
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		String strSiPkid = (String) req.getParameter("siPKID");
		if (iUserId != null && strSiPkid != null)
		{
			Long siPkid = new Long(strSiPkid);
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_TXN;
			atObj.foreignTable1 = SuppInvoiceBean.TABLENAME;
			atObj.foreignKey1 = siPkid;
			atObj.remarks = "supplier: supp-invoice-cancel ";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}// end of DoEditInvoiceFromSupplier
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.procurement;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoEditInvoiceFromSupplier implements Action
{
	String strClassName = "DoEditInvoiceFromSupplier";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		String strKeyword = req.getParameter("strKeyword");
		if (strKeyword != null)
		{
			req.setAttribute("strKeyword", strKeyword);
		}
		// If the form name is null,
		if (formName == null)
		{
			return new ActionRouter("supp-invoice-edit-1-page");
		}
		if (formName.equals("searchSuppInvoice"))
		{
			fnSearchSuppInvoice(servlet, req, res);
			return new ActionRouter("supp-invoice-edit-1-page");
		}
		// If the form has been filled and user submit to create
		// a new purchaseOrder, do the necessary checking
		if (formName.equals("selectSuppInvoice"))
		{
			String strSiPkid = req.getParameter("siPkid");
			req.setAttribute("siPkid", strSiPkid);
			Long siPkid = new Long(strSiPkid);
			SuppInvoiceObject siObj = SuppInvoiceNut.getObject(siPkid);
			req.setAttribute("siObj", siObj);
			return new ActionRouter("supp-invoice-edit-2-page");
		}
		if (formName.equals("editSuppInvoice"))
		{
			fnEditSuppInvoice(servlet, req, res);
			String strSiPkid = req.getParameter("siPkid");
			req.setAttribute("siPkid", strSiPkid);
			Long siPkid = new Long(strSiPkid);
			SuppInvoiceObject siObj = SuppInvoiceNut.getObject(siPkid);
			req.setAttribute("siObj", siObj);
			return new ActionRouter("supp-invoice-edit-2-page");
		}
		if (formName.equals("cancelSuppInvoice"))
		{
			fnCancelSuppInvoice(servlet, req, res);
			fnAuditTrail(servlet, req, res);
			return new ActionRouter("supp-invoice-edit-1-page");
		}
		Log.printVerbose(strClassName + ": returning default ActionRouter");
		return new ActionRouter("supp-invoice-edit-1-page");
	}

	// /////////////////////////////////////////////////////////////////
	// //
	protected void fnEditSuppInvoice(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strSiPkid = req.getParameter("siPkid");
		String invoiceNumber = req.getParameter("invoiceNumber");
		String amountDiscount = req.getParameter("amountDiscount");
		String amountNet = req.getParameter("amountNet");
		String dateReceive = req.getParameter("dateReceive");
		String dateInvoice = req.getParameter("dateInvoice");
		String dateDue = req.getParameter("dateDue");
		try
		{
			BigDecimal bdAmountDiscount = new BigDecimal(amountDiscount);
			BigDecimal bdAmountNet = new BigDecimal(amountNet);
			Timestamp tsDateReceive = TimeFormat.createTimestamp(dateReceive);
			Timestamp tsDateInvoice = TimeFormat.createTimestamp(dateInvoice);
			Timestamp tsDateDue = TimeFormat.createTimestamp(dateDue);
			Long siPkid = new Long(strSiPkid);
			SuppInvoice siEJB = SuppInvoiceNut.getHandle(siPkid);
			siEJB.setInvoiceNumber(invoiceNumber);
			siEJB.setAmountDiscount(bdAmountDiscount);
			siEJB.setAmountNet(bdAmountNet);
			siEJB.setDateReceive(tsDateReceive);
			siEJB.setDateInvoice(tsDateInvoice);
			siEJB.setDateDue(tsDateDue);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			return;
		}
	}

	// /////////////////////////////////////////////////////////////////
	// //
	protected void fnCancelSuppInvoice(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strSiPkid = req.getParameter("siPkid");
		Long siPkid = new Long(strSiPkid);
		SuppInvoice siEJB = SuppInvoiceNut.getHandle(siPkid);
		if (siEJB == null)
		{
			String errMsg = " The SuppInvoice " + strSiPkid + " does not exist ";
			req.setAttribute("errMsg", errMsg);
			return;
		}
		try
		{
			String status = siEJB.getStatus();
			if (!status.equals(SuppInvoiceBean.STATUS_ACTIVE))
			{
				String errMsg = " You can cancel active only." + " Supp Invoice " + strSiPkid + " is not active ";
				req.setAttribute("errMsg", errMsg);
			}
			String state = siEJB.getState();
			if (state.equals(SuppInvoiceBean.ST_POSTED))
			{
				Vector vecNatObj = NominalAccountTxnNut.getValueObjectsGiven(NominalAccountTxnBean.FOREIGN_TABLE,
						NominalAccountTxnBean.FT_SUPP_INVOICE, NominalAccountTxnBean.FOREIGN_KEY, strSiPkid);
				for (int count = 0; count < vecNatObj.size(); count++)
				{
					NominalAccountTxnObject natObj = (NominalAccountTxnObject) vecNatObj.get(count);
					NominalAccountTxn natEJB = NominalAccountTxnNut.getHandle(natObj.pkid);
					NominalAccount naEJB = NominalAccountNut.getHandle(natEJB.getNominalAccount());
					// BigDecimal bdBalance = naEJB.getAmount();
					// bdBalance = bdBalance.add(siEJB.getAmountNet());
					// naEJB.setAmount(bdBalance);
					naEJB.addAmount(naEJB.getAmount());
					natEJB.setStatus(NominalAccountTxnBean.STATUS_CANCELLED);
				} // end for
			} // end if
			siEJB.setStatus(SuppInvoiceBean.STATUS_CANCELLED);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	// /////////////////////////////////////////////////////////////////
	// //
	protected void fnSearchSuppInvoice(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strKeyword = (String) req.getParameter("strKeyword");
		Vector vecSiObj = SuppInvoiceNut.getValueObjectsILike(strKeyword);
		req.setAttribute("vecSiObj", vecSiObj);
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		String strSiPkid = (String) req.getParameter("siPKID");
		if (iUserId != null && strSiPkid != null)
		{
			Long siPkid = new Long(strSiPkid);
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_TXN;
			atObj.foreignTable1 = SuppInvoiceBean.TABLENAME;
			atObj.foreignKey1 = siPkid;
			atObj.remarks = "supplier: supp-invoice-cancel ";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}// end of DoEditInvoiceFromSupplier
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.procurement;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoEditInvoiceFromSupplier implements Action
{
	String strClassName = "DoEditInvoiceFromSupplier";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		String strKeyword = req.getParameter("strKeyword");
		if (strKeyword != null)
		{
			req.setAttribute("strKeyword", strKeyword);
		}
		// If the form name is null,
		if (formName == null)
		{
			return new ActionRouter("supp-invoice-edit-1-page");
		}
		if (formName.equals("searchSuppInvoice"))
		{
			fnSearchSuppInvoice(servlet, req, res);
			return new ActionRouter("supp-invoice-edit-1-page");
		}
		// If the form has been filled and user submit to create
		// a new purchaseOrder, do the necessary checking
		if (formName.equals("selectSuppInvoice"))
		{
			String strSiPkid = req.getParameter("siPkid");
			req.setAttribute("siPkid", strSiPkid);
			Long siPkid = new Long(strSiPkid);
			SuppInvoiceObject siObj = SuppInvoiceNut.getObject(siPkid);
			req.setAttribute("siObj", siObj);
			return new ActionRouter("supp-invoice-edit-2-page");
		}
		if (formName.equals("editSuppInvoice"))
		{
			fnEditSuppInvoice(servlet, req, res);
			String strSiPkid = req.getParameter("siPkid");
			req.setAttribute("siPkid", strSiPkid);
			Long siPkid = new Long(strSiPkid);
			SuppInvoiceObject siObj = SuppInvoiceNut.getObject(siPkid);
			req.setAttribute("siObj", siObj);
			return new ActionRouter("supp-invoice-edit-2-page");
		}
		if (formName.equals("cancelSuppInvoice"))
		{
			fnCancelSuppInvoice(servlet, req, res);
			fnAuditTrail(servlet, req, res);
			return new ActionRouter("supp-invoice-edit-1-page");
		}
		Log.printVerbose(strClassName + ": returning default ActionRouter");
		return new ActionRouter("supp-invoice-edit-1-page");
	}

	// /////////////////////////////////////////////////////////////////
	// //
	protected void fnEditSuppInvoice(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strSiPkid = req.getParameter("siPkid");
		String invoiceNumber = req.getParameter("invoiceNumber");
		String amountDiscount = req.getParameter("amountDiscount");
		String amountNet = req.getParameter("amountNet");
		String dateReceive = req.getParameter("dateReceive");
		String dateInvoice = req.getParameter("dateInvoice");
		String dateDue = req.getParameter("dateDue");
		try
		{
			BigDecimal bdAmountDiscount = new BigDecimal(amountDiscount);
			BigDecimal bdAmountNet = new BigDecimal(amountNet);
			Timestamp tsDateReceive = TimeFormat.createTimestamp(dateReceive);
			Timestamp tsDateInvoice = TimeFormat.createTimestamp(dateInvoice);
			Timestamp tsDateDue = TimeFormat.createTimestamp(dateDue);
			Long siPkid = new Long(strSiPkid);
			SuppInvoice siEJB = SuppInvoiceNut.getHandle(siPkid);
			siEJB.setInvoiceNumber(invoiceNumber);
			siEJB.setAmountDiscount(bdAmountDiscount);
			siEJB.setAmountNet(bdAmountNet);
			siEJB.setDateReceive(tsDateReceive);
			siEJB.setDateInvoice(tsDateInvoice);
			siEJB.setDateDue(tsDateDue);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			return;
		}
	}

	// /////////////////////////////////////////////////////////////////
	// //
	protected void fnCancelSuppInvoice(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strSiPkid = req.getParameter("siPkid");
		Long siPkid = new Long(strSiPkid);
		SuppInvoice siEJB = SuppInvoiceNut.getHandle(siPkid);
		if (siEJB == null)
		{
			String errMsg = " The SuppInvoice " + strSiPkid + " does not exist ";
			req.setAttribute("errMsg", errMsg);
			return;
		}
		try
		{
			String status = siEJB.getStatus();
			if (!status.equals(SuppInvoiceBean.STATUS_ACTIVE))
			{
				String errMsg = " You can cancel active only." + " Supp Invoice " + strSiPkid + " is not active ";
				req.setAttribute("errMsg", errMsg);
			}
			String state = siEJB.getState();
			if (state.equals(SuppInvoiceBean.ST_POSTED))
			{
				Vector vecNatObj = NominalAccountTxnNut.getValueObjectsGiven(NominalAccountTxnBean.FOREIGN_TABLE,
						NominalAccountTxnBean.FT_SUPP_INVOICE, NominalAccountTxnBean.FOREIGN_KEY, strSiPkid);
				for (int count = 0; count < vecNatObj.size(); count++)
				{
					NominalAccountTxnObject natObj = (NominalAccountTxnObject) vecNatObj.get(count);
					NominalAccountTxn natEJB = NominalAccountTxnNut.getHandle(natObj.pkid);
					NominalAccount naEJB = NominalAccountNut.getHandle(natEJB.getNominalAccount());
					// BigDecimal bdBalance = naEJB.getAmount();
					// bdBalance = bdBalance.add(siEJB.getAmountNet());
					// naEJB.setAmount(bdBalance);
					naEJB.addAmount(naEJB.getAmount());
					natEJB.setStatus(NominalAccountTxnBean.STATUS_CANCELLED);
				} // end for
			} // end if
			siEJB.setStatus(SuppInvoiceBean.STATUS_CANCELLED);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	// /////////////////////////////////////////////////////////////////
	// //
	protected void fnSearchSuppInvoice(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strKeyword = (String) req.getParameter("strKeyword");
		Vector vecSiObj = SuppInvoiceNut.getValueObjectsILike(strKeyword);
		req.setAttribute("vecSiObj", vecSiObj);
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		String strSiPkid = (String) req.getParameter("siPKID");
		if (iUserId != null && strSiPkid != null)
		{
			Long siPkid = new Long(strSiPkid);
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_TXN;
			atObj.foreignTable1 = SuppInvoiceBean.TABLENAME;
			atObj.foreignKey1 = siPkid;
			atObj.remarks = "supplier: supp-invoice-cancel ";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}// end of DoEditInvoiceFromSupplier
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.procurement;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoEditInvoiceFromSupplier implements Action
{
	String strClassName = "DoEditInvoiceFromSupplier";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		String strKeyword = req.getParameter("strKeyword");
		if (strKeyword != null)
		{
			req.setAttribute("strKeyword", strKeyword);
		}
		// If the form name is null,
		if (formName == null)
		{
			return new ActionRouter("supp-invoice-edit-1-page");
		}
		if (formName.equals("searchSuppInvoice"))
		{
			fnSearchSuppInvoice(servlet, req, res);
			return new ActionRouter("supp-invoice-edit-1-page");
		}
		// If the form has been filled and user submit to create
		// a new purchaseOrder, do the necessary checking
		if (formName.equals("selectSuppInvoice"))
		{
			String strSiPkid = req.getParameter("siPkid");
			req.setAttribute("siPkid", strSiPkid);
			Long siPkid = new Long(strSiPkid);
			SuppInvoiceObject siObj = SuppInvoiceNut.getObject(siPkid);
			req.setAttribute("siObj", siObj);
			return new ActionRouter("supp-invoice-edit-2-page");
		}
		if (formName.equals("editSuppInvoice"))
		{
			fnEditSuppInvoice(servlet, req, res);
			String strSiPkid = req.getParameter("siPkid");
			req.setAttribute("siPkid", strSiPkid);
			Long siPkid = new Long(strSiPkid);
			SuppInvoiceObject siObj = SuppInvoiceNut.getObject(siPkid);
			req.setAttribute("siObj", siObj);
			return new ActionRouter("supp-invoice-edit-2-page");
		}
		if (formName.equals("cancelSuppInvoice"))
		{
			fnCancelSuppInvoice(servlet, req, res);
			fnAuditTrail(servlet, req, res);
			return new ActionRouter("supp-invoice-edit-1-page");
		}
		Log.printVerbose(strClassName + ": returning default ActionRouter");
		return new ActionRouter("supp-invoice-edit-1-page");
	}

	// /////////////////////////////////////////////////////////////////
	// //
	protected void fnEditSuppInvoice(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strSiPkid = req.getParameter("siPkid");
		String invoiceNumber = req.getParameter("invoiceNumber");
		String amountDiscount = req.getParameter("amountDiscount");
		String amountNet = req.getParameter("amountNet");
		String dateReceive = req.getParameter("dateReceive");
		String dateInvoice = req.getParameter("dateInvoice");
		String dateDue = req.getParameter("dateDue");
		try
		{
			BigDecimal bdAmountDiscount = new BigDecimal(amountDiscount);
			BigDecimal bdAmountNet = new BigDecimal(amountNet);
			Timestamp tsDateReceive = TimeFormat.createTimestamp(dateReceive);
			Timestamp tsDateInvoice = TimeFormat.createTimestamp(dateInvoice);
			Timestamp tsDateDue = TimeFormat.createTimestamp(dateDue);
			Long siPkid = new Long(strSiPkid);
			SuppInvoice siEJB = SuppInvoiceNut.getHandle(siPkid);
			siEJB.setInvoiceNumber(invoiceNumber);
			siEJB.setAmountDiscount(bdAmountDiscount);
			siEJB.setAmountNet(bdAmountNet);
			siEJB.setDateReceive(tsDateReceive);
			siEJB.setDateInvoice(tsDateInvoice);
			siEJB.setDateDue(tsDateDue);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			return;
		}
	}

	// /////////////////////////////////////////////////////////////////
	// //
	protected void fnCancelSuppInvoice(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strSiPkid = req.getParameter("siPkid");
		Long siPkid = new Long(strSiPkid);
		SuppInvoice siEJB = SuppInvoiceNut.getHandle(siPkid);
		if (siEJB == null)
		{
			String errMsg = " The SuppInvoice " + strSiPkid + " does not exist ";
			req.setAttribute("errMsg", errMsg);
			return;
		}
		try
		{
			String status = siEJB.getStatus();
			if (!status.equals(SuppInvoiceBean.STATUS_ACTIVE))
			{
				String errMsg = " You can cancel active only." + " Supp Invoice " + strSiPkid + " is not active ";
				req.setAttribute("errMsg", errMsg);
			}
			String state = siEJB.getState();
			if (state.equals(SuppInvoiceBean.ST_POSTED))
			{
				Vector vecNatObj = NominalAccountTxnNut.getValueObjectsGiven(NominalAccountTxnBean.FOREIGN_TABLE,
						NominalAccountTxnBean.FT_SUPP_INVOICE, NominalAccountTxnBean.FOREIGN_KEY, strSiPkid);
				for (int count = 0; count < vecNatObj.size(); count++)
				{
					NominalAccountTxnObject natObj = (NominalAccountTxnObject) vecNatObj.get(count);
					NominalAccountTxn natEJB = NominalAccountTxnNut.getHandle(natObj.pkid);
					NominalAccount naEJB = NominalAccountNut.getHandle(natEJB.getNominalAccount());
					// BigDecimal bdBalance = naEJB.getAmount();
					// bdBalance = bdBalance.add(siEJB.getAmountNet());
					// naEJB.setAmount(bdBalance);
					naEJB.addAmount(naEJB.getAmount());
					natEJB.setStatus(NominalAccountTxnBean.STATUS_CANCELLED);
				} // end for
			} // end if
			siEJB.setStatus(SuppInvoiceBean.STATUS_CANCELLED);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	// /////////////////////////////////////////////////////////////////
	// //
	protected void fnSearchSuppInvoice(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strKeyword = (String) req.getParameter("strKeyword");
		Vector vecSiObj = SuppInvoiceNut.getValueObjectsILike(strKeyword);
		req.setAttribute("vecSiObj", vecSiObj);
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		String strSiPkid = (String) req.getParameter("siPKID");
		if (iUserId != null && strSiPkid != null)
		{
			Long siPkid = new Long(strSiPkid);
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_TXN;
			atObj.foreignTable1 = SuppInvoiceBean.TABLENAME;
			atObj.foreignKey1 = siPkid;
			atObj.remarks = "supplier: supp-invoice-cancel ";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}// end of DoEditInvoiceFromSupplier
