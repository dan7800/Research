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

public class DoCollectionProcessing extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		req.setAttribute("focusElement", req.getParameter("focusElement"));
		req.setAttribute("anchorName", req.getParameter("anchorName"));

		System.out.println("focusElement :"+req.getParameter("focusElement"));
		
		String fwdPage = req.getParameter("fwdPage");
		if(fwdPage==null)
		{ fwdPage = "dist-collection-processing-form-page";}	

		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		else if (formName.equals("getList"))
		{
			try
			{
				fnGetListing(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		else if (formName.equals("setPaymentDetails"))
		{
			try
			{
				fnSetPaymentDetails(servlet, req, res);
			} 
			catch(Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		else if (formName.equals("refreshList"))
		{
			try
			{
				fnRefreshList(servlet, req, res);
//				return new ActionRouter("dist-collection-processing-child-page");
			} 
			catch(Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
			}

		}
		else if (formName.equals("setInternalFlag"))
		{
			try
			{
				fnSetInternalFlag(servlet, req, res);
			} 
			catch(Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		else if (formName.equals("setCallRemarks"))
		{
			try
			{
				fnSetCallRemarks(servlet, req, res);
			} 
			catch(Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		
		return new ActionRouter(fwdPage);
	}

	private void fnSetPaymentDetails(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session.getAttribute("dist-collection-processing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			Integer branchId = new Integer(req.getParameter("branch"));
			String paymentStatus = req.getParameter("paymentStatus");
			String receiptRemarks = req.getParameter("receiptRemarks");
			String receiptMode = req.getParameter("receiptMode");
			String paymentStatusList = req.getParameter("paymentStatusList");
			String receiptApprovalCode = req.getParameter("receiptApprovalCode");
			String internalFlag = req.getParameter("internalFlag");
			 
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
					documentTrail = DocumentProcessingItemNut.appendDocTrail("PAY AT",oldBranch.description,newBranch.description, 
														documentTrail);
				}
				if (!soObj.receiptRemarks.equals(receiptRemarks))
				{
					documentTrail = DocumentProcessingItemNut.appendDocTrail("RCT-RMKS",soObj.receiptRemarks,receiptRemarks, 
														documentTrail);
				}
				
				System.out.println("Payment Status : "+paymentStatusList);
				System.out.println("SO OBJ Payment Status : "+soObj.statusPayment);
				
				if (!soObj.statusPayment.equals(paymentStatusList))
				{
					documentTrail = DocumentProcessingItemNut.appendDocTrail("PAY-STA",soObj.statusPayment,paymentStatusList, 
														documentTrail);
				}
				if (!soObj.receiptApprovalCode.equals(receiptApprovalCode))
				{
					documentTrail = DocumentProcessingItemNut.appendDocTrail("ApprovalCode",soObj.receiptApprovalCode,receiptApprovalCode, 
														documentTrail);
				}
				soObj.receiptBranch = branchId;
				soObj.receiptRemarks = receiptRemarks;
				// soObj.receiptMode = receiptMode;
				soObj.statusPayment = paymentStatusList;
				soObj.receiptApprovalCode = receiptApprovalCode;
				soObj.flagInternal = internalFlag;
				
				System.out.println("internalFlag :"+internalFlag);
				
				CardPaymentConfigObject cpcObj = CardPaymentConfigNut.getObject(new Integer(receiptMode));
				try
				{
					if (receiptMode_hasChanged != null && receiptMode_hasChanged.equals("true"))
					{
						if (cpcObj != null)
						{
							documentTrail = DocumentProcessingItemNut.appendDocTrail("PayMode",
														soObj.receiptMode,cpcObj.paymentMode, documentTrail);
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
					dpiObj.processType = "UPDATE-ORDER";
					dpiObj.category = "UPDATE-PAYMENT-DETAILS";
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
					dpiObj.time = TimeFormat.getTimestamp();
					DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);
				}
			}
		} catch (Exception ex)
		{
			throw new Exception(" Unable to change the Receipt Branch! ");
		}
		ppForm.getList();
	}

	private void fnSetInternalFlag(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session.getAttribute("dist-collection-processing-form");
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			String internalFlag = req.getParameter("internalFlag");
			 
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);		
			if (soEJB != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				soObj.flagInternal = internalFlag;
				soEJB.setObject(soObj);			
				
				System.out.println("internalFlag :"+internalFlag);
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
		String paymentRemarks = req.getParameter("paymentRemarks");
		String paymentStatus = req.getParameter("paymentStatus");
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session.getAttribute("dist-collection-processing-form");
		if (ppForm == null)
		{
			Integer userId = (Integer) session.getAttribute("userId");
			ppForm = new PaymentProcessingForm(userId);
			session.setAttribute("dist-collection-processing-form", ppForm);
		}
		ppForm.setBranchId(branch);
		ppForm.setDateType(dateType);
		ppForm.setDateRange(dateFrom, dateTo);
		ppForm.setSortOrder(sortOrder);
		ppForm.setPaymentModeId(new Integer(paymentModeId));
		ppForm.setPaymentRemarks(paymentRemarks);
		ppForm.setPaymentStatus(paymentStatus);
		ppForm.getList();
	}

	private void fnRefreshList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
      PaymentProcessingForm ppForm = (PaymentProcessingForm) session
            .getAttribute("dist-collection-processing-form");
      if (ppForm == null)
      {
         Integer userId = (Integer) session.getAttribute("userId");
         ppForm = new PaymentProcessingForm(userId);
         session.setAttribute("dist-collection-processing-form", ppForm);
      }
      ppForm.getList();
   }

	private void fnSetCallRemarks(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session.getAttribute("dist-collection-processing-form");
		
		try
		{
			Long soPkid = new Long(req.getParameter("soPkid"));
			String callRemarks = req.getParameter("callRemarks");
			callRemarks = StringManup.truncateNicely(callRemarks, 3000); 
			
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);		
			if (soEJB != null)
			{
				SalesOrderIndexObject soObj = soEJB.getObject();
				soObj.remarks2 = callRemarks;
				soEJB.setObject(soObj);			
				
				System.out.println("callRemarks :"+callRemarks);
			}
		} catch (Exception ex)
		{
		}
		
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

public class DoCollectionProcessing extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		req.setAttribute("focusElement", req.getParameter("focusElement"));
		req.setAttribute("anchorName", req.getParameter("anchorName"));

		String fwdPage = req.getParameter("fwdPage");
		if(fwdPage==null)
		{ fwdPage = "dist-collection-processing-form-page";}	

		if (formName == null)
		{
			return new ActionRouter(fwdPage);
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
		if (formName.equals("setPaymentDetails"))
		{
			try
			{
				fnSetPaymentDetails(servlet, req, res);
			} 
			catch(Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
			}
		}

		if(formName.equals("refreshList"))
		{
			try
			{
				fnRefreshList(servlet, req, res);
				return new ActionRouter("dist-collection-processing-child-page");
			} 
			catch(Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
			}

		}

		return new ActionRouter(fwdPage);
	}

	private void fnSetPaymentDetails(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		PaymentProcessingForm ppForm = (PaymentProcessingForm) session
				.getAttribute("dist-collection-processing-form");
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
					documentTrail = DocumentProcessingItemNut.appendDocTrail("PAY AT",oldBranch.description,newBranch.description, 
														documentTrail);
				}
				if (!soObj.receiptRemarks.equals(receiptRemarks))
				{
					documentTrail = DocumentProcessingItemNut.appendDocTrail("RCT-RMKS",soObj.receiptRemarks,receiptRemarks, 
														documentTrail);
				}
				if (!soObj.receiptApprovalCode.equals(receiptApprovalCode))
				{
					documentTrail = DocumentProcessingItemNut.appendDocTrail("ApprovalCode",soObj.receiptApprovalCode,receiptApprovalCode, 
														documentTrail);
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
							documentTrail = DocumentProcessingItemNut.appendDocTrail("PayMode",
														soObj.receiptMode,cpcObj.paymentMode, documentTrail);
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
					dpiObj.processType = "UPDATE-ORDER";
					dpiObj.category = "UPDATE-PAYMENT-DETAILS";
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
					dpiObj.time = TimeFormat.getTimestamp();
					DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);
				}
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
				.getAttribute("dist-collection-processing-form");
		if (ppForm == null)
		{
			Integer userId = (Integer) session.getAttribute("userId");
			ppForm = new PaymentProcessingForm(userId);
			session.setAttribute("dist-collection-processing-form", ppForm);
		}
		ppForm.setBranchId(branch);
		ppForm.setDateType(dateType);
		ppForm.setDateRange(dateFrom, dateTo);
		ppForm.setSortOrder(sortOrder);
		ppForm.setPaymentModeId(new Integer(paymentModeId));
		ppForm.setPaymentStatus(paymentStatus);
		ppForm.getList();
	}

	private void fnRefreshList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
      PaymentProcessingForm ppForm = (PaymentProcessingForm) session
            .getAttribute("dist-collection-processing-form");
      if (ppForm == null)
      {
         Integer userId = (Integer) session.getAttribute("userId");
         ppForm = new PaymentProcessingForm(userId);
         session.setAttribute("dist-collection-processing-form", ppForm);
      }
      ppForm.getList();
   }


}
