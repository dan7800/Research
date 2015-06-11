/*========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.pos;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import javax.sql.*;
import java.math.*;
import java.lang.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoEditReceipt implements Action
{
	private String strClassName = "DoEditReceipt";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// String receiptPKID = (String) req.getParameter("receiptPKID");
		String formName = (String) req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("pos-edit-receipt-page");
		}
		if (formName.equals("searchReceipt"))
		{
			String receiptID = (String) req.getParameter("receiptPKID");
			Log.printVerbose(strClassName + ":" + "receiptId :" + receiptID);
			fnGetSearchResults(servlet, req, res, receiptID);
		}
		if (formName.equals("receiptEdit"))
		{
			fnEditReceipt(servlet, req, res);
			String receiptID = (String) req.getParameter("receiptPKID");
			Log.printVerbose(strClassName + ":" + "receiptId :" + receiptID);
			fnGetSearchResults(servlet, req, res, receiptID);
		}
		if (formName.equals("cancelReceipt"))
		{
			fnCancelReceipt(servlet, req, res);
			String receiptID = (String) req.getParameter("receiptPKID");
			Log.printVerbose(strClassName + ":" + "receiptId :" + receiptID);
			fnGetSearchResults(servlet, req, res, receiptID);
		}
		return new ActionRouter("pos-edit-receipt-page");
	} // end of ActionRouter

	protected void fnCancelReceipt(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		// Integer iUserId = null;
		try
		{
			String receiptID = (String) req.getParameter("receiptPKID");
			Long lRecId = new Long(receiptID);
			Receipt recEJB = ReceiptNut.getHandle(lRecId);
			String status = recEJB.getStatus();
			if (!status.equals("active"))
			{
				return;
			}
			Vector vecNatObj = NominalAccountTxnNut.getValueObjectsGiven(NominalAccountTxnBean.FOREIGN_TABLE,
					NominalAccountTxnBean.FT_CUST_RECEIPT, NominalAccountTxnBean.FOREIGN_KEY, receiptID);
			Vector vecCatObj = CashAccTxnNut
					.getValueObjectsGiven(CashAccTxnBean.FOREIGN_TABLE, CashAccTxnBean.FT_CUST_RECEIPT,
							CashAccTxnBean.FOREIGN_KEY, receiptID, (String) null, (String) null);
			for (int count = 0; count < vecNatObj.size(); count++)
			{
				NominalAccountTxnObject natObj = (NominalAccountTxnObject) vecNatObj.get(count);
				NominalAccountTxn natEJB = NominalAccountTxnNut.getHandle(natObj.pkid);
				NominalAccount naEJB = NominalAccountNut.getHandle(natEJB.getNominalAccount());
				// BigDecimal bdBalance = naEJB.getAmount();
				// bdBalance = bdBalance.add(recEJB.getPaymentAmount());
				// naEJB.setAmount(naEJB.getAmount());
				naEJB.addAmount(recEJB.getPaymentAmount());
				natEJB.setStatus(NominalAccountTxnBean.STATUS_CANCELLED);
			}
			for (int count = 0; count < vecCatObj.size(); count++)
			{
				CashAccTxnObject catObj = (CashAccTxnObject) vecCatObj.get(count);
				CashAccTxn catEJB = CashAccTxnNut.getHandle(catObj.pkid);
				catEJB.setStatus(CashAccTxnBean.STATUS_CANCELLED);
			}
			recEJB.setStatus(ReceiptBean.STATUS_CANCELLED);
			fnAuditTrail(servlet, req, res);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	protected void fnEditReceipt(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		// Integer iUserId = null;
		try
		{
			String receiptID = (String) req.getParameter("receiptPKID");
			Long lRecId = new Long(receiptID);
			Receipt recEJB = ReceiptNut.getHandle(lRecId);
			String salesTxnId = (String) req.getParameter("salesTxnId");
			Long lSalesTxnId = new Long(salesTxnId);
			String strBankId = (String) req.getParameter("bankId");
			Integer bankId = new Integer(strBankId);
			CashAccountObject newCaObj = CashAccountNut.getObject(bankId);
			String editAmountStr = (String) req.getParameter("editAmountStr");
			// Integer editCustAcc = new
			// Integer((String)req.getParameter("editAccountId"));
			// BigDecimal editAmountPaid = new
			// BigDecimal((String)req.getParameter("editAmountPaid"));
			String editPaymentMethod = (String) req.getParameter("editPaymentMethod");
			Timestamp editPaymentTime = TimeFormat.createTimestamp((String) req.getParameter("editPaymentTime"));
			String editPaymentRemarks = (String) req.getParameter("editPaymentRemarks");
			String editChequeNumber = (String) req.getParameter("editChequeNumber");
			String editStatus = (String) req.getParameter("editStatus");
			// Integer editStaff = new Integer
			// ((String)req.getParameter("editStaff"));
			ReceiptObject rctObj = (ReceiptObject) ReceiptNut.getObject(lRecId);
			Vector vecCAT = (Vector) CashAccTxnNut.getValueObjectsGiven(CashAccTxnBean.FOREIGN_TABLE,
					ReceiptBean.TABLENAME, CashAccTxnBean.FOREIGN_KEY, rctObj.pkid.toString(), (String) null,
					(String) null);
			// / update cash Acc Txn
			for (int cnt1 = 0; cnt1 < vecCAT.size(); cnt1++)
			{
				CashAccTxnObject catObj = (CashAccTxnObject) vecCAT.get(cnt1);
				CashAccTxn catEJB = CashAccTxnNut.getHandle(catObj.pkid);
				catEJB.setGLCodeDebit(newCaObj.accountType);
				catEJB.setTxnTime(editPaymentTime);
				catEJB.setAccTo(bankId);
				catEJB.setAccFrom(bankId);
			}
			String strUserName = (String) req.getParameter("newUserNameUpdate");
			UserHome lUserHome = UserNut.getHome();
			User lUser = UserNut.getHandle(lUserHome, strUserName);
			Integer iUserId = (Integer) lUser.getUserId();
			// // update nominal Acc Txn
			Vector vecNAT = (Vector) NominalAccountTxnNut.getValueObjectsGiven(NominalAccountTxnBean.FOREIGN_TABLE,
					ReceiptBean.TABLENAME, NominalAccountTxnBean.FOREIGN_KEY, rctObj.pkid.toString());
			for (int cnt1 = 0; cnt1 < vecNAT.size(); cnt1++)
			{
				NominalAccountTxnObject natObj = (NominalAccountTxnObject) vecNAT.get(cnt1);
				NominalAccountTxn natEJB = NominalAccountTxnNut.getHandle(natObj.pkid);
				natEJB.setGLCodeDebit(newCaObj.accountType);
				natEJB.setTimeParam1(editPaymentTime);
			}
			try
			{
				recEJB.setBankId(bankId);
				recEJB.setAmountStr(editAmountStr);
				// recEJB.setCustAccount(editCustAcc);
				// recEJB.setPaymentAmount(editAmountPaid);
				recEJB.setSalesTxnId(lSalesTxnId);
				recEJB.setPaymentMethod(editPaymentMethod);
				recEJB.setPaymentTime(editPaymentTime);
				recEJB.setPaymentRemarks(editPaymentRemarks);
				recEJB.setChequeNumber(editChequeNumber);
				recEJB.setUserIdUpdate(iUserId);
				// recEJB.setStatus(editStatus);
			} catch (Exception ex)
			{
				ex.printStackTrace();
				Log.printDebug(" ERROR " + ex.getMessage());
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	protected void fnGetSearchResults(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			String receiptID)
	{
		String funcName = "fnGetSearchResults()";
		Log.printVerbose(strClassName + ":" + funcName + "-" + "receiptPKID = " + receiptID);
		Long receiptId = new Long(receiptID);
		ReceiptObject receiptObj = (ReceiptObject) ReceiptNut.getObject(receiptId);
		req.setAttribute("receiptObj", receiptObj);
		CashAccountObject caObj = CashAccountNut.getObject(receiptObj.bankId);
		// / get the list of cashbook for this PC Center
		Vector vecCashAcc = CashAccountNut.getValueObjectsGiven(CashAccountBean.PC_CENTER, caObj.pcCenter.toString(),
				CashAccountBean.STATUS, CashAccountBean.STATUS_ACTIVE);
		req.setAttribute("vecCashAcc", vecCashAcc);
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		String receiptID = (String) req.getParameter("receiptPKID");
		if (iUserId != null && receiptID != null)
		{
			Long iReceipt = new Long(receiptID);
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_TXN;
			atObj.foreignTable1 = ReceiptBean.TABLENAME;
			atObj.foreignKey1 = iReceipt;
			atObj.remarks = "pos: cust-receipt-cancel ";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}// end of class DoEditReceipt
/*========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.pos;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import javax.sql.*;
import java.math.*;
import java.lang.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoEditReceipt implements Action
{
	private String strClassName = "DoEditReceipt";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// String receiptPKID = (String) req.getParameter("receiptPKID");
		String formName = (String) req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("pos-edit-receipt-page");
		}
		if (formName.equals("searchReceipt"))
		{
			String receiptID = (String) req.getParameter("receiptPKID");
			Log.printVerbose(strClassName + ":" + "receiptId :" + receiptID);
			fnGetSearchResults(servlet, req, res, receiptID);
		}
		if (formName.equals("receiptEdit"))
		{
			fnEditReceipt(servlet, req, res);
			String receiptID = (String) req.getParameter("receiptPKID");
			Log.printVerbose(strClassName + ":" + "receiptId :" + receiptID);
			fnGetSearchResults(servlet, req, res, receiptID);
		}
		if (formName.equals("cancelReceipt"))
		{
			fnCancelReceipt(servlet, req, res);
			String receiptID = (String) req.getParameter("receiptPKID");
			Log.printVerbose(strClassName + ":" + "receiptId :" + receiptID);
			fnGetSearchResults(servlet, req, res, receiptID);
		}
		return new ActionRouter("pos-edit-receipt-page");
	} // end of ActionRouter

	protected void fnCancelReceipt(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		// Integer iUserId = null;
		try
		{
			String receiptID = (String) req.getParameter("receiptPKID");
			Long lRecId = new Long(receiptID);
			Receipt recEJB = ReceiptNut.getHandle(lRecId);
			String status = recEJB.getStatus();
			if (!status.equals("active"))
			{
				return;
			}
			Vector vecNatObj = NominalAccountTxnNut.getValueObjectsGiven(NominalAccountTxnBean.FOREIGN_TABLE,
					NominalAccountTxnBean.FT_CUST_RECEIPT, NominalAccountTxnBean.FOREIGN_KEY, receiptID);
			Vector vecCatObj = CashAccTxnNut
					.getValueObjectsGiven(CashAccTxnBean.FOREIGN_TABLE, CashAccTxnBean.FT_CUST_RECEIPT,
							CashAccTxnBean.FOREIGN_KEY, receiptID, (String) null, (String) null);
			for (int count = 0; count < vecNatObj.size(); count++)
			{
				NominalAccountTxnObject natObj = (NominalAccountTxnObject) vecNatObj.get(count);
				NominalAccountTxn natEJB = NominalAccountTxnNut.getHandle(natObj.pkid);
				NominalAccount naEJB = NominalAccountNut.getHandle(natEJB.getNominalAccount());
				// BigDecimal bdBalance = naEJB.getAmount();
				// bdBalance = bdBalance.add(recEJB.getPaymentAmount());
				// naEJB.setAmount(naEJB.getAmount());
				naEJB.addAmount(recEJB.getPaymentAmount());
				natEJB.setStatus(NominalAccountTxnBean.STATUS_CANCELLED);
			}
			for (int count = 0; count < vecCatObj.size(); count++)
			{
				CashAccTxnObject catObj = (CashAccTxnObject) vecCatObj.get(count);
				CashAccTxn catEJB = CashAccTxnNut.getHandle(catObj.pkid);
				catEJB.setStatus(CashAccTxnBean.STATUS_CANCELLED);
			}
			recEJB.setStatus(ReceiptBean.STATUS_CANCELLED);
			fnAuditTrail(servlet, req, res);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	protected void fnEditReceipt(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		// Integer iUserId = null;
		try
		{
			String receiptID = (String) req.getParameter("receiptPKID");
			Long lRecId = new Long(receiptID);
			Receipt recEJB = ReceiptNut.getHandle(lRecId);
			String salesTxnId = (String) req.getParameter("salesTxnId");
			Long lSalesTxnId = new Long(salesTxnId);
			String strBankId = (String) req.getParameter("bankId");
			Integer bankId = new Integer(strBankId);
			CashAccountObject newCaObj = CashAccountNut.getObject(bankId);
			String editAmountStr = (String) req.getParameter("editAmountStr");
			// Integer editCustAcc = new
			// Integer((String)req.getParameter("editAccountId"));
			// BigDecimal editAmountPaid = new
			// BigDecimal((String)req.getParameter("editAmountPaid"));
			String editPaymentMethod = (String) req.getParameter("editPaymentMethod");
			Timestamp editPaymentTime = TimeFormat.createTimestamp((String) req.getParameter("editPaymentTime"));
			String editPaymentRemarks = (String) req.getParameter("editPaymentRemarks");
			String editChequeNumber = (String) req.getParameter("editChequeNumber");
			String editStatus = (String) req.getParameter("editStatus");
			// Integer editStaff = new Integer
			// ((String)req.getParameter("editStaff"));
			ReceiptObject rctObj = (ReceiptObject) ReceiptNut.getObject(lRecId);
			Vector vecCAT = (Vector) CashAccTxnNut.getValueObjectsGiven(CashAccTxnBean.FOREIGN_TABLE,
					ReceiptBean.TABLENAME, CashAccTxnBean.FOREIGN_KEY, rctObj.pkid.toString(), (String) null,
					(String) null);
			// / update cash Acc Txn
			for (int cnt1 = 0; cnt1 < vecCAT.size(); cnt1++)
			{
				CashAccTxnObject catObj = (CashAccTxnObject) vecCAT.get(cnt1);
				CashAccTxn catEJB = CashAccTxnNut.getHandle(catObj.pkid);
				catEJB.setGLCodeDebit(newCaObj.accountType);
				catEJB.setTxnTime(editPaymentTime);
				catEJB.setAccTo(bankId);
				catEJB.setAccFrom(bankId);
			}
			String strUserName = (String) req.getParameter("newUserNameUpdate");
			UserHome lUserHome = UserNut.getHome();
			User lUser = UserNut.getHandle(lUserHome, strUserName);
			Integer iUserId = (Integer) lUser.getUserId();
			// // update nominal Acc Txn
			Vector vecNAT = (Vector) NominalAccountTxnNut.getValueObjectsGiven(NominalAccountTxnBean.FOREIGN_TABLE,
					ReceiptBean.TABLENAME, NominalAccountTxnBean.FOREIGN_KEY, rctObj.pkid.toString());
			for (int cnt1 = 0; cnt1 < vecNAT.size(); cnt1++)
			{
				NominalAccountTxnObject natObj = (NominalAccountTxnObject) vecNAT.get(cnt1);
				NominalAccountTxn natEJB = NominalAccountTxnNut.getHandle(natObj.pkid);
				natEJB.setGLCodeDebit(newCaObj.accountType);
				natEJB.setTimeParam1(editPaymentTime);
			}
			try
			{
				recEJB.setBankId(bankId);
				recEJB.setAmountStr(editAmountStr);
				// recEJB.setCustAccount(editCustAcc);
				// recEJB.setPaymentAmount(editAmountPaid);
				recEJB.setSalesTxnId(lSalesTxnId);
				recEJB.setPaymentMethod(editPaymentMethod);
				recEJB.setPaymentTime(editPaymentTime);
				recEJB.setPaymentRemarks(editPaymentRemarks);
				recEJB.setChequeNumber(editChequeNumber);
				recEJB.setUserIdUpdate(iUserId);
				// recEJB.setStatus(editStatus);
			} catch (Exception ex)
			{
				ex.printStackTrace();
				Log.printDebug(" ERROR " + ex.getMessage());
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	protected void fnGetSearchResults(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			String receiptID)
	{
		String funcName = "fnGetSearchResults()";
		Log.printVerbose(strClassName + ":" + funcName + "-" + "receiptPKID = " + receiptID);
		Long receiptId = new Long(receiptID);
		ReceiptObject receiptObj = (ReceiptObject) ReceiptNut.getObject(receiptId);
		req.setAttribute("receiptObj", receiptObj);
		CashAccountObject caObj = CashAccountNut.getObject(receiptObj.bankId);
		// / get the list of cashbook for this PC Center
		Vector vecCashAcc = CashAccountNut.getValueObjectsGiven(CashAccountBean.PC_CENTER, caObj.pcCenter.toString(),
				CashAccountBean.STATUS, CashAccountBean.STATUS_ACTIVE);
		req.setAttribute("vecCashAcc", vecCashAcc);
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		String receiptID = (String) req.getParameter("receiptPKID");
		if (iUserId != null && receiptID != null)
		{
			Long iReceipt = new Long(receiptID);
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_TXN;
			atObj.foreignTable1 = ReceiptBean.TABLENAME;
			atObj.foreignKey1 = iReceipt;
			atObj.remarks = "pos: cust-receipt-cancel ";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}// end of class DoEditReceipt
/*========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.pos;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import javax.sql.*;
import java.math.*;
import java.lang.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoEditReceipt implements Action
{
	private String strClassName = "DoEditReceipt";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// String receiptPKID = (String) req.getParameter("receiptPKID");
		String formName = (String) req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("pos-edit-receipt-page");
		}
		if (formName.equals("searchReceipt"))
		{
			String receiptID = (String) req.getParameter("receiptPKID");
			Log.printVerbose(strClassName + ":" + "receiptId :" + receiptID);
			fnGetSearchResults(servlet, req, res, receiptID);
		}
		if (formName.equals("receiptEdit"))
		{
			fnEditReceipt(servlet, req, res);
			String receiptID = (String) req.getParameter("receiptPKID");
			Log.printVerbose(strClassName + ":" + "receiptId :" + receiptID);
			fnGetSearchResults(servlet, req, res, receiptID);
		}
		if (formName.equals("cancelReceipt"))
		{
			fnCancelReceipt(servlet, req, res);
			String receiptID = (String) req.getParameter("receiptPKID");
			Log.printVerbose(strClassName + ":" + "receiptId :" + receiptID);
			fnGetSearchResults(servlet, req, res, receiptID);
		}
		return new ActionRouter("pos-edit-receipt-page");
	} // end of ActionRouter

	protected void fnCancelReceipt(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		// Integer iUserId = null;
		try
		{
			String receiptID = (String) req.getParameter("receiptPKID");
			Long lRecId = new Long(receiptID);
			Receipt recEJB = ReceiptNut.getHandle(lRecId);
			String status = recEJB.getStatus();
			if (!status.equals("active"))
			{
				return;
			}
			Vector vecNatObj = NominalAccountTxnNut.getValueObjectsGiven(NominalAccountTxnBean.FOREIGN_TABLE,
					NominalAccountTxnBean.FT_CUST_RECEIPT, NominalAccountTxnBean.FOREIGN_KEY, receiptID);
			Vector vecCatObj = CashAccTxnNut
					.getValueObjectsGiven(CashAccTxnBean.FOREIGN_TABLE, CashAccTxnBean.FT_CUST_RECEIPT,
							CashAccTxnBean.FOREIGN_KEY, receiptID, (String) null, (String) null);
			for (int count = 0; count < vecNatObj.size(); count++)
			{
				NominalAccountTxnObject natObj = (NominalAccountTxnObject) vecNatObj.get(count);
				NominalAccountTxn natEJB = NominalAccountTxnNut.getHandle(natObj.pkid);
				NominalAccount naEJB = NominalAccountNut.getHandle(natEJB.getNominalAccount());
				// BigDecimal bdBalance = naEJB.getAmount();
				// bdBalance = bdBalance.add(recEJB.getPaymentAmount());
				// naEJB.setAmount(naEJB.getAmount());
				naEJB.addAmount(recEJB.getPaymentAmount());
				natEJB.setStatus(NominalAccountTxnBean.STATUS_CANCELLED);
			}
			for (int count = 0; count < vecCatObj.size(); count++)
			{
				CashAccTxnObject catObj = (CashAccTxnObject) vecCatObj.get(count);
				CashAccTxn catEJB = CashAccTxnNut.getHandle(catObj.pkid);
				catEJB.setStatus(CashAccTxnBean.STATUS_CANCELLED);
			}
			recEJB.setStatus(ReceiptBean.STATUS_CANCELLED);
			fnAuditTrail(servlet, req, res);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	protected void fnEditReceipt(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		// Integer iUserId = null;
		try
		{
			String receiptID = (String) req.getParameter("receiptPKID");
			Long lRecId = new Long(receiptID);
			Receipt recEJB = ReceiptNut.getHandle(lRecId);
			String salesTxnId = (String) req.getParameter("salesTxnId");
			Long lSalesTxnId = new Long(salesTxnId);
			String strBankId = (String) req.getParameter("bankId");
			Integer bankId = new Integer(strBankId);
			CashAccountObject newCaObj = CashAccountNut.getObject(bankId);
			String editAmountStr = (String) req.getParameter("editAmountStr");
			// Integer editCustAcc = new
			// Integer((String)req.getParameter("editAccountId"));
			// BigDecimal editAmountPaid = new
			// BigDecimal((String)req.getParameter("editAmountPaid"));
			String editPaymentMethod = (String) req.getParameter("editPaymentMethod");
			Timestamp editPaymentTime = TimeFormat.createTimestamp((String) req.getParameter("editPaymentTime"));
			String editPaymentRemarks = (String) req.getParameter("editPaymentRemarks");
			String editChequeNumber = (String) req.getParameter("editChequeNumber");
			String editStatus = (String) req.getParameter("editStatus");
			// Integer editStaff = new Integer
			// ((String)req.getParameter("editStaff"));
			ReceiptObject rctObj = (ReceiptObject) ReceiptNut.getObject(lRecId);
			Vector vecCAT = (Vector) CashAccTxnNut.getValueObjectsGiven(CashAccTxnBean.FOREIGN_TABLE,
					ReceiptBean.TABLENAME, CashAccTxnBean.FOREIGN_KEY, rctObj.pkid.toString(), (String) null,
					(String) null);
			// / update cash Acc Txn
			for (int cnt1 = 0; cnt1 < vecCAT.size(); cnt1++)
			{
				CashAccTxnObject catObj = (CashAccTxnObject) vecCAT.get(cnt1);
				CashAccTxn catEJB = CashAccTxnNut.getHandle(catObj.pkid);
				catEJB.setGLCodeDebit(newCaObj.accountType);
				catEJB.setTxnTime(editPaymentTime);
				catEJB.setAccTo(bankId);
				catEJB.setAccFrom(bankId);
			}
			String strUserName = (String) req.getParameter("newUserNameUpdate");
			UserHome lUserHome = UserNut.getHome();
			User lUser = UserNut.getHandle(lUserHome, strUserName);
			Integer iUserId = (Integer) lUser.getUserId();
			// // update nominal Acc Txn
			Vector vecNAT = (Vector) NominalAccountTxnNut.getValueObjectsGiven(NominalAccountTxnBean.FOREIGN_TABLE,
					ReceiptBean.TABLENAME, NominalAccountTxnBean.FOREIGN_KEY, rctObj.pkid.toString());
			for (int cnt1 = 0; cnt1 < vecNAT.size(); cnt1++)
			{
				NominalAccountTxnObject natObj = (NominalAccountTxnObject) vecNAT.get(cnt1);
				NominalAccountTxn natEJB = NominalAccountTxnNut.getHandle(natObj.pkid);
				natEJB.setGLCodeDebit(newCaObj.accountType);
				natEJB.setTimeParam1(editPaymentTime);
			}
			try
			{
				recEJB.setBankId(bankId);
				recEJB.setAmountStr(editAmountStr);
				// recEJB.setCustAccount(editCustAcc);
				// recEJB.setPaymentAmount(editAmountPaid);
				recEJB.setSalesTxnId(lSalesTxnId);
				recEJB.setPaymentMethod(editPaymentMethod);
				recEJB.setPaymentTime(editPaymentTime);
				recEJB.setPaymentRemarks(editPaymentRemarks);
				recEJB.setChequeNumber(editChequeNumber);
				recEJB.setUserIdUpdate(iUserId);
				// recEJB.setStatus(editStatus);
			} catch (Exception ex)
			{
				ex.printStackTrace();
				Log.printDebug(" ERROR " + ex.getMessage());
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	protected void fnGetSearchResults(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			String receiptID)
	{
		String funcName = "fnGetSearchResults()";
		Log.printVerbose(strClassName + ":" + funcName + "-" + "receiptPKID = " + receiptID);
		Long receiptId = new Long(receiptID);
		ReceiptObject receiptObj = (ReceiptObject) ReceiptNut.getObject(receiptId);
		req.setAttribute("receiptObj", receiptObj);
		CashAccountObject caObj = CashAccountNut.getObject(receiptObj.bankId);
		// / get the list of cashbook for this PC Center
		Vector vecCashAcc = CashAccountNut.getValueObjectsGiven(CashAccountBean.PC_CENTER, caObj.pcCenter.toString(),
				CashAccountBean.STATUS, CashAccountBean.STATUS_ACTIVE);
		req.setAttribute("vecCashAcc", vecCashAcc);
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		String receiptID = (String) req.getParameter("receiptPKID");
		if (iUserId != null && receiptID != null)
		{
			Long iReceipt = new Long(receiptID);
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_TXN;
			atObj.foreignTable1 = ReceiptBean.TABLENAME;
			atObj.foreignKey1 = iReceipt;
			atObj.remarks = "pos: cust-receipt-cancel ";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}// end of class DoEditReceipt
