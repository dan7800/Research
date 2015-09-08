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
import java.util.*;
import javax.servlet.http.*;

import com.vlee.ejb.customer.*;
import com.vlee.ejb.accounting.*;
import com.vlee.bean.distribution.*;
import com.vlee.util.*;

public class DoDebtCollectionPool extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("dist-debt-collection-pool-page");
		}
		if (formName.equals("create"))
		{
			try
			{
				fnCreateDCP(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("print"))
		{
			try
			{
				Log.printVerbose("testing");
				return new ActionRouter("dist-debt-collection-print-page");
				// return new ActionRouter("dist-debt-collection-pool-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setDocProc"))
		{
			try
			{
				fnSetDocProc(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("saveDetails"))
		{
			try
			{
				fnSaveDetails(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		if (formName.equals("saveApprovalCode"))
		{
			try
			{
				fnSaveApprovalCode(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		if (formName.equals("updateOrderReceiptRemarks"))
		{
			try
			{
				fnUpdateReceiptRemarks(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("remove"))
		{
			try
			{
				fnRemove(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("addOrder"))
		{
			try
			{
				fnAddOrder(servlet, req, res);
				req.setAttribute("success-add-order", "true");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("dist-debt-collection-pool-page");
	}

	private void fnAddOrder(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		String[] soPkid = req.getParameterValues("soPkid");
		for (int cnt = 0; cnt < soPkid.length; cnt++)
		{
			try
			{
				Long lPkid = new Long(soPkid[cnt]);
				edcf.fnAddOrder(lPkid);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	private void fnRemove(HttpServlet serlvet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		String dpiPkid = req.getParameter("dpiPkid");
		try
		{
			Long lDpiPkid = new Long(dpiPkid);
			edcf.fnRemoveOrder(lDpiPkid);
		} catch (Exception ex)
		{
			throw ex;
		}
	}

	private void fnSaveApprovalCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String soPkid = req.getParameter("soPkid");
		String soApprovalCode = req.getParameter("soApprovalCode");
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		try
		{
			Long lSoPkid = new Long(soPkid);
			edcf.setApprovalCode(lSoPkid, soApprovalCode);
		} catch (Exception ex)
		{
			throw ex;
		}
	}

	private void fnUpdateReceiptRemarks(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String soPkid = req.getParameter("soPkid");
		String receiptRemarks = req.getParameter("receiptRemarks");
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		QueryObject queryPayRmks = new QueryObject(new String[] {
				StringTemplateBean.CATEGORY + " ='" + StringTemplateBean.CAT_PAYMENT_REMARKS + "' ",
				StringTemplateBean.CONTENT + " ='" + receiptRemarks + "' " });
		queryPayRmks.setOrder(" ORDER BY " + StringTemplateBean.SORT);
		Vector vecPayRmks = new Vector(StringTemplateNut.getObjects(queryPayRmks));
		if (vecPayRmks != null && vecPayRmks.size() > 0)
		{
			StringTemplateObject stObj = (StringTemplateObject) vecPayRmks.get(0);
			try
			{
				Long lSoPkid = new Long(soPkid);
				edcf.setReceiptStatus(lSoPkid, receiptRemarks, stObj.description);
			} catch (Exception ex)
			{
				throw ex;
			}
		}
	}

	private void fnSetDocProc(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String dpPkid = req.getParameter("dpPkid");
		Long lPkid = new Long(dpPkid);
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		edcf.setDebtCollectionPool(lPkid);
	}

	private void fnSaveDetails(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		String dpPkid = req.getParameter("dpPkid");
		try
		{
			Long lDpPkid = new Long(dpPkid);
			DocumentProcessingObject dpObj = edcf.getDebtCollectionPool();
			if (lDpPkid.equals(dpObj.pkid))
			{
				String description1 = req.getParameter("description1");
				String description2 = req.getParameter("description2");
				String remarks = req.getParameter("remarks");
				String userPerform = req.getParameter("userPerform");
				Integer iUserPerform = new Integer(userPerform);
				String dateScheduled = req.getParameter("dateScheduled");
				edcf.setDebtCollectionPoolDetails(iUserPerform, description1, description2, remarks);
				edcf.setDateScheduled(dateScheduled);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void fnCreateDCP(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		DocumentProcessingObject dpObj = new DocumentProcessingObject();
		dpObj.pkid = new Long(0); // Primary Key
		dpObj.module = DocumentProcessingBean.MODULE_DISTRIBUTION;
		dpObj.processType = DocumentProcessingBean.PROCESS_DEBT_COLLECTION;
		dpObj.category = "";
		// dpObj.auditLevel = new Integer(0);
		dpObj.userCreate = userId;
		// dpObj.userPerform = new Integer(0);
		// dpObj.userConfirm = new Integer(0);
		dpObj.description1 = "";
		dpObj.description2 = "";
		dpObj.remarks = "";
		dpObj.timeCreated = TimeFormat.getTimestamp();
		dpObj.timeScheduled = TimeFormat.getTimestamp();
		dpObj.timeCompleted = dpObj.timeScheduled;
		// dpObj.state = DocumentProcessingBean.STATE_CREATED;
		// dpObj.status = DocumentProcessingBean.STATUS_ACTIVE;
		DocumentProcessing dpEJB = DocumentProcessingNut.fnCreate(dpObj);
		if (dpEJB != null)
		{
			EditDebtCollectionForm edcf = (EditDebtCollectionForm) session
					.getAttribute("dist-debt-collection-pool-form");
			edcf.setDebtCollectionPool(dpObj.pkid);
		}
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
import java.util.*;
import javax.servlet.http.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.accounting.*;
import com.vlee.bean.distribution.*;
import com.vlee.util.*;

public class DoDebtCollectionPool extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("dist-debt-collection-pool-page");
		}
		if (formName.equals("create"))
		{
			try
			{
				fnCreateDCP(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("print"))
		{
			try
			{
				Log.printVerbose("testing");
				return new ActionRouter("dist-debt-collection-print-page");
				// return new ActionRouter("dist-debt-collection-pool-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setDocProc"))
		{
			try
			{
				fnSetDocProc(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("saveDetails"))
		{
			try
			{
				fnSaveDetails(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		if (formName.equals("saveApprovalCode"))
		{
			try
			{
				fnSaveApprovalCode(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		if (formName.equals("updateOrderReceiptRemarks"))
		{
			try
			{
				fnUpdateReceiptRemarks(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("remove"))
		{
			try
			{
				fnRemove(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("dist-debt-collection-pool-page");
	}

	private void fnRemove(HttpServlet serlvet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		String dpiPkid = req.getParameter("dpiPkid");
		try
		{
			Long lDpiPkid = new Long(dpiPkid);
			edcf.fnRemoveOrder(lDpiPkid);
		} catch (Exception ex)
		{
			throw ex;
		}
	}

	private void fnSaveApprovalCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String soPkid = req.getParameter("soPkid");
		String soApprovalCode = req.getParameter("soApprovalCode");
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		try
		{
			Long lSoPkid = new Long(soPkid);
			edcf.setApprovalCode(lSoPkid, soApprovalCode);
		} catch (Exception ex)
		{
			throw ex;
		}
	}

	private void fnUpdateReceiptRemarks(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String soPkid = req.getParameter("soPkid");
		String receiptRemarks = req.getParameter("receiptRemarks");
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		QueryObject queryPayRmks = new QueryObject(new String[] {
				StringTemplateBean.CATEGORY + " ='" + StringTemplateBean.CAT_PAYMENT_REMARKS + "' ",
				StringTemplateBean.CONTENT + " ='" + receiptRemarks + "' " });
		queryPayRmks.setOrder(" ORDER BY " + StringTemplateBean.SORT);
		Vector vecPayRmks = new Vector(StringTemplateNut.getObjects(queryPayRmks));
		if (vecPayRmks != null && vecPayRmks.size() > 0)
		{
			StringTemplateObject stObj = (StringTemplateObject) vecPayRmks.get(0);
			try
			{
				Long lSoPkid = new Long(soPkid);
				edcf.setReceiptStatus(lSoPkid, receiptRemarks, stObj.description);
			} catch (Exception ex)
			{
				throw ex;
			}
		}
	}

	private void fnSetDocProc(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String dpPkid = req.getParameter("dpPkid");
		Long lPkid = new Long(dpPkid);
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		edcf.setDebtCollectionPool(lPkid);
	}

	private void fnSaveDetails(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		String dpPkid = req.getParameter("dpPkid");
		try
		{
			Long lDpPkid = new Long(dpPkid);
			DocumentProcessingObject dpObj = edcf.getDebtCollectionPool();
			if (lDpPkid.equals(dpObj.pkid))
			{
				String description1 = req.getParameter("description1");
				String description2 = req.getParameter("description2");
				String remarks = req.getParameter("remarks");
				String userPerform = req.getParameter("userPerform");
				Integer iUserPerform = new Integer(userPerform);
				String dateScheduled = req.getParameter("dateScheduled");
				edcf.setDebtCollectionPoolDetails(iUserPerform, description1, description2, remarks);
				edcf.setDateScheduled(dateScheduled);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void fnCreateDCP(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		DocumentProcessingObject dpObj = new DocumentProcessingObject();
		dpObj.pkid = new Long(0); // Primary Key
		dpObj.module = DocumentProcessingBean.MODULE_DISTRIBUTION;
		dpObj.processType = DocumentProcessingBean.PROCESS_DEBT_COLLECTION;
		dpObj.category = "";
		// dpObj.auditLevel = new Integer(0);
		dpObj.userCreate = userId;
		// dpObj.userPerform = new Integer(0);
		// dpObj.userConfirm = new Integer(0);
		dpObj.description1 = "";
		dpObj.description2 = "";
		dpObj.remarks = "";
		dpObj.timeCreated = TimeFormat.getTimestamp();
		dpObj.timeScheduled = TimeFormat.getTimestamp();
		dpObj.timeCompleted = dpObj.timeScheduled;
		// dpObj.state = DocumentProcessingBean.STATE_CREATED;
		// dpObj.status = DocumentProcessingBean.STATUS_ACTIVE;
		DocumentProcessing dpEJB = DocumentProcessingNut.fnCreate(dpObj);
		if (dpEJB != null)
		{
			EditDebtCollectionForm edcf = (EditDebtCollectionForm) session
					.getAttribute("dist-debt-collection-pool-form");
			edcf.setDebtCollectionPool(dpObj.pkid);
		}
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
import java.util.*;
import javax.servlet.http.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.accounting.*;
import com.vlee.bean.distribution.*;
import com.vlee.util.*;

public class DoDebtCollectionPool extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("dist-debt-collection-pool-page");
		}
		if (formName.equals("create"))
		{
			try
			{
				fnCreateDCP(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("print"))
		{
			try
			{
				Log.printVerbose("testing");
				return new ActionRouter("dist-debt-collection-print-page");
				// return new ActionRouter("dist-debt-collection-pool-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setDocProc"))
		{
			try
			{
				fnSetDocProc(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("saveDetails"))
		{
			try
			{
				fnSaveDetails(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		if (formName.equals("saveApprovalCode"))
		{
			try
			{
				fnSaveApprovalCode(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		if (formName.equals("updateOrderReceiptRemarks"))
		{
			try
			{
				fnUpdateReceiptRemarks(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("remove"))
		{
			try
			{
				fnRemove(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("dist-debt-collection-pool-page");
	}

	private void fnRemove(HttpServlet serlvet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		String dpiPkid = req.getParameter("dpiPkid");
		try
		{
			Long lDpiPkid = new Long(dpiPkid);
			edcf.fnRemoveOrder(lDpiPkid);
		} catch (Exception ex)
		{
			throw ex;
		}
	}

	private void fnSaveApprovalCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String soPkid = req.getParameter("soPkid");
		String soApprovalCode = req.getParameter("soApprovalCode");
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		try
		{
			Long lSoPkid = new Long(soPkid);
			edcf.setApprovalCode(lSoPkid, soApprovalCode);
		} catch (Exception ex)
		{
			throw ex;
		}
	}

	private void fnUpdateReceiptRemarks(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String soPkid = req.getParameter("soPkid");
		String receiptRemarks = req.getParameter("receiptRemarks");
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		QueryObject queryPayRmks = new QueryObject(new String[] {
				StringTemplateBean.CATEGORY + " ='" + StringTemplateBean.CAT_PAYMENT_REMARKS + "' ",
				StringTemplateBean.CONTENT + " ='" + receiptRemarks + "' " });
		queryPayRmks.setOrder(" ORDER BY " + StringTemplateBean.SORT);
		Vector vecPayRmks = new Vector(StringTemplateNut.getObjects(queryPayRmks));
		if (vecPayRmks != null && vecPayRmks.size() > 0)
		{
			StringTemplateObject stObj = (StringTemplateObject) vecPayRmks.get(0);
			try
			{
				Long lSoPkid = new Long(soPkid);
				edcf.setReceiptStatus(lSoPkid, receiptRemarks, stObj.description);
			} catch (Exception ex)
			{
				throw ex;
			}
		}
	}

	private void fnSetDocProc(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String dpPkid = req.getParameter("dpPkid");
		Long lPkid = new Long(dpPkid);
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		edcf.setDebtCollectionPool(lPkid);
	}

	private void fnSaveDetails(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		String dpPkid = req.getParameter("dpPkid");
		try
		{
			Long lDpPkid = new Long(dpPkid);
			DocumentProcessingObject dpObj = edcf.getDebtCollectionPool();
			if (lDpPkid.equals(dpObj.pkid))
			{
				String description1 = req.getParameter("description1");
				String description2 = req.getParameter("description2");
				String remarks = req.getParameter("remarks");
				String userPerform = req.getParameter("userPerform");
				Integer iUserPerform = new Integer(userPerform);
				String dateScheduled = req.getParameter("dateScheduled");
				edcf.setDebtCollectionPoolDetails(iUserPerform, description1, description2, remarks);
				edcf.setDateScheduled(dateScheduled);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void fnCreateDCP(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		DocumentProcessingObject dpObj = new DocumentProcessingObject();
		dpObj.pkid = new Long(0); // Primary Key
		dpObj.module = DocumentProcessingBean.MODULE_DISTRIBUTION;
		dpObj.processType = DocumentProcessingBean.PROCESS_DEBT_COLLECTION;
		dpObj.category = "";
		// dpObj.auditLevel = new Integer(0);
		dpObj.userCreate = userId;
		// dpObj.userPerform = new Integer(0);
		// dpObj.userConfirm = new Integer(0);
		dpObj.description1 = "";
		dpObj.description2 = "";
		dpObj.remarks = "";
		dpObj.timeCreated = TimeFormat.getTimestamp();
		dpObj.timeScheduled = TimeFormat.getTimestamp();
		dpObj.timeCompleted = dpObj.timeScheduled;
		// dpObj.state = DocumentProcessingBean.STATE_CREATED;
		// dpObj.status = DocumentProcessingBean.STATUS_ACTIVE;
		DocumentProcessing dpEJB = DocumentProcessingNut.fnCreate(dpObj);
		if (dpEJB != null)
		{
			EditDebtCollectionForm edcf = (EditDebtCollectionForm) session
					.getAttribute("dist-debt-collection-pool-form");
			edcf.setDebtCollectionPool(dpObj.pkid);
		}
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
import java.util.*;
import javax.servlet.http.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.accounting.*;
import com.vlee.bean.distribution.*;
import com.vlee.util.*;

public class DoDebtCollectionPool extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("dist-debt-collection-pool-page");
		}
		if (formName.equals("create"))
		{
			try
			{
				fnCreateDCP(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("print"))
		{
			try
			{
				Log.printVerbose("testing");
				return new ActionRouter("dist-debt-collection-print-page");
				// return new ActionRouter("dist-debt-collection-pool-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setDocProc"))
		{
			try
			{
				fnSetDocProc(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("saveDetails"))
		{
			try
			{
				fnSaveDetails(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		if (formName.equals("saveApprovalCode"))
		{
			try
			{
				fnSaveApprovalCode(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		if (formName.equals("updateOrderReceiptRemarks"))
		{
			try
			{
				fnUpdateReceiptRemarks(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("remove"))
		{
			try
			{
				fnRemove(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("dist-debt-collection-pool-page");
	}

	private void fnRemove(HttpServlet serlvet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		String dpiPkid = req.getParameter("dpiPkid");
		try
		{
			Long lDpiPkid = new Long(dpiPkid);
			edcf.fnRemoveOrder(lDpiPkid);
		} catch (Exception ex)
		{
			throw ex;
		}
	}

	private void fnSaveApprovalCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String soPkid = req.getParameter("soPkid");
		String soApprovalCode = req.getParameter("soApprovalCode");
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		try
		{
			Long lSoPkid = new Long(soPkid);
			edcf.setApprovalCode(lSoPkid, soApprovalCode);
		} catch (Exception ex)
		{
			throw ex;
		}
	}

	private void fnUpdateReceiptRemarks(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String soPkid = req.getParameter("soPkid");
		String receiptRemarks = req.getParameter("receiptRemarks");
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		QueryObject queryPayRmks = new QueryObject(new String[] {
				StringTemplateBean.CATEGORY + " ='" + StringTemplateBean.CAT_PAYMENT_REMARKS + "' ",
				StringTemplateBean.CONTENT + " ='" + receiptRemarks + "' " });
		queryPayRmks.setOrder(" ORDER BY " + StringTemplateBean.SORT);
		Vector vecPayRmks = new Vector(StringTemplateNut.getObjects(queryPayRmks));
		if (vecPayRmks != null && vecPayRmks.size() > 0)
		{
			StringTemplateObject stObj = (StringTemplateObject) vecPayRmks.get(0);
			try
			{
				Long lSoPkid = new Long(soPkid);
				edcf.setReceiptStatus(lSoPkid, receiptRemarks, stObj.description);
			} catch (Exception ex)
			{
				throw ex;
			}
		}
	}

	private void fnSetDocProc(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String dpPkid = req.getParameter("dpPkid");
		Long lPkid = new Long(dpPkid);
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		edcf.setDebtCollectionPool(lPkid);
	}

	private void fnSaveDetails(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		String dpPkid = req.getParameter("dpPkid");
		try
		{
			Long lDpPkid = new Long(dpPkid);
			DocumentProcessingObject dpObj = edcf.getDebtCollectionPool();
			if (lDpPkid.equals(dpObj.pkid))
			{
				String description1 = req.getParameter("description1");
				String description2 = req.getParameter("description2");
				String remarks = req.getParameter("remarks");
				String userPerform = req.getParameter("userPerform");
				Integer iUserPerform = new Integer(userPerform);
				String dateScheduled = req.getParameter("dateScheduled");
				edcf.setDebtCollectionPoolDetails(iUserPerform, description1, description2, remarks);
				edcf.setDateScheduled(dateScheduled);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void fnCreateDCP(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		DocumentProcessingObject dpObj = new DocumentProcessingObject();
		dpObj.pkid = new Long(0); // Primary Key
		dpObj.module = DocumentProcessingBean.MODULE_DISTRIBUTION;
		dpObj.processType = DocumentProcessingBean.PROCESS_DEBT_COLLECTION;
		dpObj.category = "";
		// dpObj.auditLevel = new Integer(0);
		dpObj.userCreate = userId;
		// dpObj.userPerform = new Integer(0);
		// dpObj.userConfirm = new Integer(0);
		dpObj.description1 = "";
		dpObj.description2 = "";
		dpObj.remarks = "";
		dpObj.timeCreated = TimeFormat.getTimestamp();
		dpObj.timeScheduled = TimeFormat.getTimestamp();
		dpObj.timeCompleted = dpObj.timeScheduled;
		// dpObj.state = DocumentProcessingBean.STATE_CREATED;
		// dpObj.status = DocumentProcessingBean.STATUS_ACTIVE;
		DocumentProcessing dpEJB = DocumentProcessingNut.fnCreate(dpObj);
		if (dpEJB != null)
		{
			EditDebtCollectionForm edcf = (EditDebtCollectionForm) session
					.getAttribute("dist-debt-collection-pool-form");
			edcf.setDebtCollectionPool(dpObj.pkid);
		}
	}
}
