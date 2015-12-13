/*==========================================================
 *
 * Copyright © of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.distribution;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.math.*;
import java.sql.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.util.*;
import com.vlee.bean.distribution.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoPaymentProcessingListing extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		req.setAttribute("focusElement", req.getParameter("focusElement"));
		req.setAttribute("anchorName", req.getParameter("anchorName"));
		if (formName == null)
		{
			return new ActionRouter("dist-payment-processing-listing-page");
		}
		if (formName.equals("getList"))
		{
			try
			{
				fnGetListing(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		if (formName.equals("setReceiptBranch"))
		{
			try
			{
				fnSetReceiptBranch(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setReceiptRemarks"))
		{
			try
			{
				fnSetReceiptRemarks(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setReceiptMode"))
		{
			try
			{
				fnSetReceiptMode(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setPaymentStatus"))
		{
			try
			{
				fnSetPaymentStatus(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setApprovalCode"))
		{
			try
			{
				fnSetApprovalCode(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setPaymentDetails"))
		{
			try
			{
				fnSetPaymentDetails(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("dist-payment-processing-listing-page");
	}

	private void fnSetReceiptBranch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			Integer branchId = new Integer(req.getParameter("branch"));
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			if (soEJB != null && branchId != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				soObj.receiptBranch = branchId;
				soEJB.setObject(soObj);
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnSetPaymentDetails(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			Integer branchId = new Integer(req.getParameter("branch"));
			String receiptRemarks = req.getParameter("receiptRemarks");
			String receiptMode = req.getParameter("receiptMode");
			String paymentStatusList = req.getParameter("paymentStatusList");
			String receiptApprovalCode = req.getParameter("receiptApprovalCode");
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			String receiptMode_hasChanged = req.getParameter("receiptMode_hasChanged");
			String receiptRemarks_hasChanged = req.getParameter("receiptRemarks_hasChanged");
			String documentTrail = "";
			if (soEJB != null && branchId != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				// / create document processing trail
				if (!soObj.receiptBranch.equals(branchId))
				{
					BranchObject oldBranch = BranchNut.getObject(soObj.receiptBranch);
					BranchObject newBranch = BranchNut.getObject(branchId);
					documentTrail += " Branch: " + oldBranch.description + " -> " + newBranch.description;
				}
				if (!soObj.receiptRemarks.equals(receiptRemarks))
				{
					documentTrail += " RctRmks: " + soObj.receiptRemarks + " -> " + receiptRemarks;
				}
				if (!soObj.receiptApprovalCode.equals(receiptApprovalCode))
				{
					documentTrail += " ApprovalCode: " + soObj.receiptApprovalCode + " -> " + receiptApprovalCode;
				}
				soObj.receiptBranch = branchId;
				soObj.receiptRemarks = receiptRemarks;
				// soObj.receiptMode = receiptMode;
				soObj.statusPayment = paymentStatusList;
				soObj.receiptApprovalCode = receiptApprovalCode;
				CardPaymentConfigObject cpcObj = CardPaymentConfigNut.getObject(new Integer(receiptMode));
				try
				{
					if (receiptMode_hasChanged != null && receiptMode_hasChanged.equals("true"))
					{
						if (cpcObj != null)
						{
							documentTrail += " PayMode : " + soObj.receiptMode + " -> " + cpcObj.paymentMode;
							soObj.receiptMode = cpcObj.paymentMode;
							soObj.receiptRemarks = cpcObj.defaultPaymentRemarks;
							soObj.statusPayment = cpcObj.defaultPaymentStatus;
						} else
						{
							soObj.receiptMode = "";
						}
					}
				} catch (Exception ex)
				{
				}
				try
				{
					if (receiptRemarks_hasChanged != null && receiptRemarks_hasChanged.equals("true"))
					{
						QueryObject queryPayRmks = new QueryObject(new String[] {
								StringTemplateBean.CATEGORY + " ='" + StringTemplateBean.CAT_PAYMENT_REMARKS + "' ",
								StringTemplateBean.CONTENT + " ='" + receiptRemarks + "' " });
						queryPayRmks.setOrder(" ORDER BY " + StringTemplateBean.SORT);
						Vector vecPayRmks = new Vector(StringTemplateNut.getObjects(queryPayRmks));
						if (vecPayRmks != null && vecPayRmks.size() > 0)
						{
							StringTemplateObject stObj = (StringTemplateObject) vecPayRmks.get(0);
							soObj.statusPayment = stObj.description;
						}
					}
				} catch (Exception ex)
				{
				}
				soEJB.setObject(soObj);
				if (documentTrail.length() > 3)
				{
					DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
					dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
					dpiObj.processType = "UPDATE_RECEIPT_STATUS";
					dpiObj.category = "";
					dpiObj.auditLevel = new Integer(0);
					dpiObj.processId = new Long(0);
					dpiObj.userid = (Integer) session.getAttribute("userId");
					dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
					dpiObj.docId = soObj.pkid;
					dpiObj.entityRef = CustAccountBean.TABLENAME;
					dpiObj.entityId = soObj.senderKey1;
					dpiObj.description1 = documentTrail;
					dpiObj.description2 = "";
					dpiObj.remarks = "";
					DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);
				}
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnSetReceiptRemarks(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			String receiptRemarks = req.getParameter("receiptRemarks");
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			if (soEJB != null && receiptRemarks != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				soObj.receiptRemarks = receiptRemarks;
				soEJB.setObject(soObj);
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnSetReceiptMode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			String receiptMode = req.getParameter("receiptMode");
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			if (soEJB != null && receiptMode != null)
			{
				CardPaymentConfigObject cpcObj = CardPaymentConfigNut.getObject(new Integer(receiptMode));
				if (cpcObj != null)
				{
					SalesOrderIndexObject soObj = soEJB.getObject();
					soObj.receiptMode = cpcObj.paymentMode;
					soEJB.setObject(soObj);
				}
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnSetPaymentStatus(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			String paymentStatus = req.getParameter("paymentStatus");
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			if (soEJB != null && paymentStatus != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				soObj.statusPayment = paymentStatus;
				soEJB.setObject(soObj);
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnSetApprovalCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			String receiptApprovalCode = req.getParameter("receiptApprovalCode");
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			if (soEJB != null && receiptApprovalCode != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				soObj.receiptApprovalCode = receiptApprovalCode;
				soEJB.setObject(soObj);
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnGetListing(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String branch = req.getParameter("branch");
		String dateType = req.getParameter("dateType");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String sortOrder = req.getParameter("sortOrder");
		String paymentModeId = req.getParameter("paymentModeId");
		String paymentStatus = req.getParameter("paymentStatus");
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		if (ppForm == null)
		{
			Integer userId = (Integer) session.getAttribute("userId");
			ppForm = new PaymentProcessingForm(userId);
			session.setAttribute("dist-payment-processing-listing-form", ppForm);
		}
		ppForm.setBranchId(branch);
		ppForm.setDateType(dateType);
		ppForm.setDateRange(dateFrom, dateTo);
		ppForm.setSortOrder(sortOrder);
		ppForm.setPaymentModeId(new Integer(paymentModeId));
		ppForm.setPaymentStatus(paymentStatus);
		ppForm.getList();
	}
}
/*==========================================================
 *
 * Copyright © of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.distribution;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.math.*;
import java.sql.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.util.*;
import com.vlee.bean.distribution.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoPaymentProcessingListing extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		req.setAttribute("focusElement", req.getParameter("focusElement"));
		req.setAttribute("anchorName", req.getParameter("anchorName"));
		if (formName == null)
		{
			return new ActionRouter("dist-payment-processing-listing-page");
		}
		if (formName.equals("getList"))
		{
			try
			{
				fnGetListing(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		if (formName.equals("setReceiptBranch"))
		{
			try
			{
				fnSetReceiptBranch(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setReceiptRemarks"))
		{
			try
			{
				fnSetReceiptRemarks(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setReceiptMode"))
		{
			try
			{
				fnSetReceiptMode(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setPaymentStatus"))
		{
			try
			{
				fnSetPaymentStatus(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setApprovalCode"))
		{
			try
			{
				fnSetApprovalCode(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setPaymentDetails"))
		{
			try
			{
				fnSetPaymentDetails(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("dist-payment-processing-listing-page");
	}

	private void fnSetReceiptBranch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			Integer branchId = new Integer(req.getParameter("branch"));
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			if (soEJB != null && branchId != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				soObj.receiptBranch = branchId;
				soEJB.setObject(soObj);
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnSetPaymentDetails(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			Integer branchId = new Integer(req.getParameter("branch"));
			String receiptRemarks = req.getParameter("receiptRemarks");
			String receiptMode = req.getParameter("receiptMode");
			String paymentStatusList = req.getParameter("paymentStatusList");
			String receiptApprovalCode = req.getParameter("receiptApprovalCode");
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			String receiptMode_hasChanged = req.getParameter("receiptMode_hasChanged");
			String receiptRemarks_hasChanged = req.getParameter("receiptRemarks_hasChanged");
			String documentTrail = "";
			if (soEJB != null && branchId != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				// / create document processing trail
				if (!soObj.receiptBranch.equals(branchId))
				{
					BranchObject oldBranch = BranchNut.getObject(soObj.receiptBranch);
					BranchObject newBranch = BranchNut.getObject(branchId);
					documentTrail += " Branch: " + oldBranch.description + " -> " + newBranch.description;
				}
				if (!soObj.receiptRemarks.equals(receiptRemarks))
				{
					documentTrail += " RctRmks: " + soObj.receiptRemarks + " -> " + receiptRemarks;
				}
				if (!soObj.receiptApprovalCode.equals(receiptApprovalCode))
				{
					documentTrail += " ApprovalCode: " + soObj.receiptApprovalCode + " -> " + receiptApprovalCode;
				}
				soObj.receiptBranch = branchId;
				soObj.receiptRemarks = receiptRemarks;
				// soObj.receiptMode = receiptMode;
				soObj.statusPayment = paymentStatusList;
				soObj.receiptApprovalCode = receiptApprovalCode;
				CardPaymentConfigObject cpcObj = CardPaymentConfigNut.getObject(new Integer(receiptMode));
				try
				{
					if (receiptMode_hasChanged != null && receiptMode_hasChanged.equals("true"))
					{
						if (cpcObj != null)
						{
							documentTrail += " PayMode : " + soObj.receiptMode + " -> " + cpcObj.paymentMode;
							soObj.receiptMode = cpcObj.paymentMode;
							soObj.receiptRemarks = cpcObj.defaultPaymentRemarks;
							soObj.statusPayment = cpcObj.defaultPaymentStatus;
						} else
						{
							soObj.receiptMode = "";
						}
					}
				} catch (Exception ex)
				{
				}
				try
				{
					if (receiptRemarks_hasChanged != null && receiptRemarks_hasChanged.equals("true"))
					{
						QueryObject queryPayRmks = new QueryObject(new String[] {
								StringTemplateBean.CATEGORY + " ='" + StringTemplateBean.CAT_PAYMENT_REMARKS + "' ",
								StringTemplateBean.CONTENT + " ='" + receiptRemarks + "' " });
						queryPayRmks.setOrder(" ORDER BY " + StringTemplateBean.SORT);
						Vector vecPayRmks = new Vector(StringTemplateNut.getObjects(queryPayRmks));
						if (vecPayRmks != null && vecPayRmks.size() > 0)
						{
							StringTemplateObject stObj = (StringTemplateObject) vecPayRmks.get(0);
							soObj.statusPayment = stObj.description;
						}
					}
				} catch (Exception ex)
				{
				}
				soEJB.setObject(soObj);
				if (documentTrail.length() > 3)
				{
					DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
					dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
					dpiObj.processType = "UPDATE_RECEIPT_STATUS";
					dpiObj.category = "";
					dpiObj.auditLevel = new Integer(0);
					dpiObj.processId = new Long(0);
					dpiObj.userid = (Integer) session.getAttribute("userId");
					dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
					dpiObj.docId = soObj.pkid;
					dpiObj.entityRef = CustAccountBean.TABLENAME;
					dpiObj.entityId = soObj.senderKey1;
					dpiObj.description1 = documentTrail;
					dpiObj.description2 = "";
					dpiObj.remarks = "";
					DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);
				}
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnSetReceiptRemarks(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			String receiptRemarks = req.getParameter("receiptRemarks");
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			if (soEJB != null && receiptRemarks != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				soObj.receiptRemarks = receiptRemarks;
				soEJB.setObject(soObj);
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnSetReceiptMode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			String receiptMode = req.getParameter("receiptMode");
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			if (soEJB != null && receiptMode != null)
			{
				CardPaymentConfigObject cpcObj = CardPaymentConfigNut.getObject(new Integer(receiptMode));
				if (cpcObj != null)
				{
					SalesOrderIndexObject soObj = soEJB.getObject();
					soObj.receiptMode = cpcObj.paymentMode;
					soEJB.setObject(soObj);
				}
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnSetPaymentStatus(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			String paymentStatus = req.getParameter("paymentStatus");
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			if (soEJB != null && paymentStatus != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				soObj.statusPayment = paymentStatus;
				soEJB.setObject(soObj);
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnSetApprovalCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			String receiptApprovalCode = req.getParameter("receiptApprovalCode");
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			if (soEJB != null && receiptApprovalCode != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				soObj.receiptApprovalCode = receiptApprovalCode;
				soEJB.setObject(soObj);
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnGetListing(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String branch = req.getParameter("branch");
		String dateType = req.getParameter("dateType");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String sortOrder = req.getParameter("sortOrder");
		String paymentModeId = req.getParameter("paymentModeId");
		String paymentStatus = req.getParameter("paymentStatus");
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		if (ppForm == null)
		{
			Integer userId = (Integer) session.getAttribute("userId");
			ppForm = new PaymentProcessingForm(userId);
			session.setAttribute("dist-payment-processing-listing-form", ppForm);
		}
		ppForm.setBranchId(branch);
		ppForm.setDateType(dateType);
		ppForm.setDateRange(dateFrom, dateTo);
		ppForm.setSortOrder(sortOrder);
		ppForm.setPaymentModeId(new Integer(paymentModeId));
		ppForm.setPaymentStatus(paymentStatus);
		ppForm.getList();
	}
}
/*==========================================================
 *
 * Copyright © of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.distribution;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.math.*;
import java.sql.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.util.*;
import com.vlee.bean.distribution.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoPaymentProcessingListing extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		req.setAttribute("focusElement", req.getParameter("focusElement"));
		req.setAttribute("anchorName", req.getParameter("anchorName"));
		if (formName == null)
		{
			return new ActionRouter("dist-payment-processing-listing-page");
		}
		if (formName.equals("getList"))
		{
			try
			{
				fnGetListing(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		if (formName.equals("setReceiptBranch"))
		{
			try
			{
				fnSetReceiptBranch(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setReceiptRemarks"))
		{
			try
			{
				fnSetReceiptRemarks(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setReceiptMode"))
		{
			try
			{
				fnSetReceiptMode(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setPaymentStatus"))
		{
			try
			{
				fnSetPaymentStatus(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setApprovalCode"))
		{
			try
			{
				fnSetApprovalCode(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setPaymentDetails"))
		{
			try
			{
				fnSetPaymentDetails(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("dist-payment-processing-listing-page");
	}

	private void fnSetReceiptBranch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			Integer branchId = new Integer(req.getParameter("branch"));
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			if (soEJB != null && branchId != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				soObj.receiptBranch = branchId;
				soEJB.setObject(soObj);
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnSetPaymentDetails(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			Integer branchId = new Integer(req.getParameter("branch"));
			String receiptRemarks = req.getParameter("receiptRemarks");
			String receiptMode = req.getParameter("receiptMode");
			String paymentStatusList = req.getParameter("paymentStatusList");
			String receiptApprovalCode = req.getParameter("receiptApprovalCode");
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			String receiptMode_hasChanged = req.getParameter("receiptMode_hasChanged");
			String receiptRemarks_hasChanged = req.getParameter("receiptRemarks_hasChanged");
			String documentTrail = "";
			if (soEJB != null && branchId != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				// / create document processing trail
				if (!soObj.receiptBranch.equals(branchId))
				{
					BranchObject oldBranch = BranchNut.getObject(soObj.receiptBranch);
					BranchObject newBranch = BranchNut.getObject(branchId);
					documentTrail += " Branch: " + oldBranch.description + " -> " + newBranch.description;
				}
				if (!soObj.receiptRemarks.equals(receiptRemarks))
				{
					documentTrail += " RctRmks: " + soObj.receiptRemarks + " -> " + receiptRemarks;
				}
				if (!soObj.receiptApprovalCode.equals(receiptApprovalCode))
				{
					documentTrail += " ApprovalCode: " + soObj.receiptApprovalCode + " -> " + receiptApprovalCode;
				}
				soObj.receiptBranch = branchId;
				soObj.receiptRemarks = receiptRemarks;
				// soObj.receiptMode = receiptMode;
				soObj.statusPayment = paymentStatusList;
				soObj.receiptApprovalCode = receiptApprovalCode;
				CardPaymentConfigObject cpcObj = CardPaymentConfigNut.getObject(new Integer(receiptMode));
				try
				{
					if (receiptMode_hasChanged != null && receiptMode_hasChanged.equals("true"))
					{
						if (cpcObj != null)
						{
							documentTrail += " PayMode : " + soObj.receiptMode + " -> " + cpcObj.paymentMode;
							soObj.receiptMode = cpcObj.paymentMode;
							soObj.receiptRemarks = cpcObj.defaultPaymentRemarks;
							soObj.statusPayment = cpcObj.defaultPaymentStatus;
						} else
						{
							soObj.receiptMode = "";
						}
					}
				} catch (Exception ex)
				{
				}
				try
				{
					if (receiptRemarks_hasChanged != null && receiptRemarks_hasChanged.equals("true"))
					{
						QueryObject queryPayRmks = new QueryObject(new String[] {
								StringTemplateBean.CATEGORY + " ='" + StringTemplateBean.CAT_PAYMENT_REMARKS + "' ",
								StringTemplateBean.CONTENT + " ='" + receiptRemarks + "' " });
						queryPayRmks.setOrder(" ORDER BY " + StringTemplateBean.SORT);
						Vector vecPayRmks = new Vector(StringTemplateNut.getObjects(queryPayRmks));
						if (vecPayRmks != null && vecPayRmks.size() > 0)
						{
							StringTemplateObject stObj = (StringTemplateObject) vecPayRmks.get(0);
							soObj.statusPayment = stObj.description;
						}
					}
				} catch (Exception ex)
				{
				}
				soEJB.setObject(soObj);
				if (documentTrail.length() > 3)
				{
					DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
					dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
					dpiObj.processType = "UPDATE_RECEIPT_STATUS";
					dpiObj.category = "";
					dpiObj.auditLevel = new Integer(0);
					dpiObj.processId = new Long(0);
					dpiObj.userid = (Integer) session.getAttribute("userId");
					dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
					dpiObj.docId = soObj.pkid;
					dpiObj.entityRef = CustAccountBean.TABLENAME;
					dpiObj.entityId = soObj.senderKey1;
					dpiObj.description1 = documentTrail;
					dpiObj.description2 = "";
					dpiObj.remarks = "";
					DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);
				}
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnSetReceiptRemarks(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			String receiptRemarks = req.getParameter("receiptRemarks");
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			if (soEJB != null && receiptRemarks != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				soObj.receiptRemarks = receiptRemarks;
				soEJB.setObject(soObj);
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnSetReceiptMode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			String receiptMode = req.getParameter("receiptMode");
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			if (soEJB != null && receiptMode != null)
			{
				CardPaymentConfigObject cpcObj = CardPaymentConfigNut.getObject(new Integer(receiptMode));
				if (cpcObj != null)
				{
					SalesOrderIndexObject soObj = soEJB.getObject();
					soObj.receiptMode = cpcObj.paymentMode;
					soEJB.setObject(soObj);
				}
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnSetPaymentStatus(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			String paymentStatus = req.getParameter("paymentStatus");
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			if (soEJB != null && paymentStatus != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				soObj.statusPayment = paymentStatus;
				soEJB.setObject(soObj);
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnSetApprovalCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			String receiptApprovalCode = req.getParameter("receiptApprovalCode");
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			if (soEJB != null && receiptApprovalCode != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				soObj.receiptApprovalCode = receiptApprovalCode;
				soEJB.setObject(soObj);
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnGetListing(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String branch = req.getParameter("branch");
		String dateType = req.getParameter("dateType");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String sortOrder = req.getParameter("sortOrder");
		String paymentModeId = req.getParameter("paymentModeId");
		String paymentStatus = req.getParameter("paymentStatus");
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		if (ppForm == null)
		{
			Integer userId = (Integer) session.getAttribute("userId");
			ppForm = new PaymentProcessingForm(userId);
			session.setAttribute("dist-payment-processing-listing-form", ppForm);
		}
		ppForm.setBranchId(branch);
		ppForm.setDateType(dateType);
		ppForm.setDateRange(dateFrom, dateTo);
		ppForm.setSortOrder(sortOrder);
		ppForm.setPaymentModeId(new Integer(paymentModeId));
		ppForm.setPaymentStatus(paymentStatus);
		ppForm.getList();
	}
}
/*==========================================================
 *
 * Copyright © of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.distribution;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.math.*;
import java.sql.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.util.*;
import com.vlee.bean.distribution.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoPaymentProcessingListing extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		req.setAttribute("focusElement", req.getParameter("focusElement"));
		req.setAttribute("anchorName", req.getParameter("anchorName"));
		if (formName == null)
		{
			return new ActionRouter("dist-payment-processing-listing-page");
		}
		if (formName.equals("getList"))
		{
			try
			{
				fnGetListing(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		if (formName.equals("setReceiptBranch"))
		{
			try
			{
				fnSetReceiptBranch(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setReceiptRemarks"))
		{
			try
			{
				fnSetReceiptRemarks(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setReceiptMode"))
		{
			try
			{
				fnSetReceiptMode(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setPaymentStatus"))
		{
			try
			{
				fnSetPaymentStatus(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setApprovalCode"))
		{
			try
			{
				fnSetApprovalCode(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setPaymentDetails"))
		{
			try
			{
				fnSetPaymentDetails(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("dist-payment-processing-listing-page");
	}

	private void fnSetReceiptBranch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			Integer branchId = new Integer(req.getParameter("branch"));
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			if (soEJB != null && branchId != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				soObj.receiptBranch = branchId;
				soEJB.setObject(soObj);
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnSetPaymentDetails(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			Integer branchId = new Integer(req.getParameter("branch"));
			String receiptRemarks = req.getParameter("receiptRemarks");
			String receiptMode = req.getParameter("receiptMode");
			String paymentStatusList = req.getParameter("paymentStatusList");
			String receiptApprovalCode = req.getParameter("receiptApprovalCode");
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			String receiptMode_hasChanged = req.getParameter("receiptMode_hasChanged");
			String receiptRemarks_hasChanged = req.getParameter("receiptRemarks_hasChanged");
			String documentTrail = "";
			if (soEJB != null && branchId != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				// / create document processing trail
				if (!soObj.receiptBranch.equals(branchId))
				{
					BranchObject oldBranch = BranchNut.getObject(soObj.receiptBranch);
					BranchObject newBranch = BranchNut.getObject(branchId);
					documentTrail += " Branch: " + oldBranch.description + " -> " + newBranch.description;
				}
				if (!soObj.receiptRemarks.equals(receiptRemarks))
				{
					documentTrail += " RctRmks: " + soObj.receiptRemarks + " -> " + receiptRemarks;
				}
				if (!soObj.receiptApprovalCode.equals(receiptApprovalCode))
				{
					documentTrail += " ApprovalCode: " + soObj.receiptApprovalCode + " -> " + receiptApprovalCode;
				}
				soObj.receiptBranch = branchId;
				soObj.receiptRemarks = receiptRemarks;
				// soObj.receiptMode = receiptMode;
				soObj.statusPayment = paymentStatusList;
				soObj.receiptApprovalCode = receiptApprovalCode;
				CardPaymentConfigObject cpcObj = CardPaymentConfigNut.getObject(new Integer(receiptMode));
				try
				{
					if (receiptMode_hasChanged != null && receiptMode_hasChanged.equals("true"))
					{
						if (cpcObj != null)
						{
							documentTrail += " PayMode : " + soObj.receiptMode + " -> " + cpcObj.paymentMode;
							soObj.receiptMode = cpcObj.paymentMode;
							soObj.receiptRemarks = cpcObj.defaultPaymentRemarks;
							soObj.statusPayment = cpcObj.defaultPaymentStatus;
						} else
						{
							soObj.receiptMode = "";
						}
					}
				} catch (Exception ex)
				{
				}
				try
				{
					if (receiptRemarks_hasChanged != null && receiptRemarks_hasChanged.equals("true"))
					{
						QueryObject queryPayRmks = new QueryObject(new String[] {
								StringTemplateBean.CATEGORY + " ='" + StringTemplateBean.CAT_PAYMENT_REMARKS + "' ",
								StringTemplateBean.CONTENT + " ='" + receiptRemarks + "' " });
						queryPayRmks.setOrder(" ORDER BY " + StringTemplateBean.SORT);
						Vector vecPayRmks = new Vector(StringTemplateNut.getObjects(queryPayRmks));
						if (vecPayRmks != null && vecPayRmks.size() > 0)
						{
							StringTemplateObject stObj = (StringTemplateObject) vecPayRmks.get(0);
							soObj.statusPayment = stObj.description;
						}
					}
				} catch (Exception ex)
				{
				}
				soEJB.setObject(soObj);
				if (documentTrail.length() > 3)
				{
					DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
					dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
					dpiObj.processType = "UPDATE_RECEIPT_STATUS";
					dpiObj.category = "";
					dpiObj.auditLevel = new Integer(0);
					dpiObj.processId = new Long(0);
					dpiObj.userid = (Integer) session.getAttribute("userId");
					dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
					dpiObj.docId = soObj.pkid;
					dpiObj.entityRef = CustAccountBean.TABLENAME;
					dpiObj.entityId = soObj.senderKey1;
					dpiObj.description1 = documentTrail;
					dpiObj.description2 = "";
					dpiObj.remarks = "";
					DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);
				}
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnSetReceiptRemarks(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			String receiptRemarks = req.getParameter("receiptRemarks");
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			if (soEJB != null && receiptRemarks != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				soObj.receiptRemarks = receiptRemarks;
				soEJB.setObject(soObj);
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnSetReceiptMode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			String receiptMode = req.getParameter("receiptMode");
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			if (soEJB != null && receiptMode != null)
			{
				CardPaymentConfigObject cpcObj = CardPaymentConfigNut.getObject(new Integer(receiptMode));
				if (cpcObj != null)
				{
					SalesOrderIndexObject soObj = soEJB.getObject();
					soObj.receiptMode = cpcObj.paymentMode;
					soEJB.setObject(soObj);
				}
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnSetPaymentStatus(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			String paymentStatus = req.getParameter("paymentStatus");
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			if (soEJB != null && paymentStatus != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				soObj.statusPayment = paymentStatus;
				soEJB.setObject(soObj);
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnSetApprovalCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			String receiptApprovalCode = req.getParameter("receiptApprovalCode");
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
			if (soEJB != null && receiptApprovalCode != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				soObj.receiptApprovalCode = receiptApprovalCode;
				soEJB.setObject(soObj);
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnGetListing(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String branch = req.getParameter("branch");
		String dateType = req.getParameter("dateType");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String sortOrder = req.getParameter("sortOrder");
		String paymentModeId = req.getParameter("paymentModeId");
		String paymentStatus = req.getParameter("paymentStatus");
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-payment-processing-listing-form");
		if (ppForm == null)
		{
			Integer userId = (Integer) session.getAttribute("userId");
			ppForm = new PaymentProcessingForm(userId);
			session.setAttribute("dist-payment-processing-listing-form", ppForm);
		}
		ppForm.setBranchId(branch);
		ppForm.setDateType(dateType);
		ppForm.setDateRange(dateFrom, dateTo);
		ppForm.setSortOrder(sortOrder);
		ppForm.setPaymentModeId(new Integer(paymentModeId));
		ppForm.setPaymentStatus(paymentStatus);
		ppForm.getList();
	}
}
