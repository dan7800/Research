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

public class DoRecordOrderComplaint extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		req.setAttribute("orderNo",req.getParameter("orderNo"));

		if(formName==null)
		{
			return new ActionRouter("dist-record-order-complaint-page");
		}

		if(formName.equals("saveComplaint"))
		{
			fnSaveComplaint(servlet,req,res);
		}

		return new ActionRouter("dist-record-order-complaint-page");
	}

	private void fnSaveComplaint(HttpServlet servlet, HttpServletRequest req,
											HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		String orderNo = req.getParameter("orderNo");
		String complaintTakenBy = req.getParameter("complaintTakenBy");
		String complaintTime = req.getParameter("complaintTime");
		String complaint = req.getParameter("complaint");
		String actionTakenBy = req.getParameter("actionTakenBy");
		String actionTakenTime = req.getParameter("actionTakenTime");
		String actionTaken = req.getParameter("actionTaken");
		String flagInternal = req.getParameter("flagInternal");

		complaint = complaint.trim();
		actionTaken = actionTaken.trim();

		try
		{
			Integer iComplaintTakenBy = new Integer(complaintTakenBy);
			Integer iActionTakenBy = new Integer(actionTakenBy);

			Long lOrder = new Long(orderNo);
			SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(lOrder);
			String oldFlagInternal = soObj.flagInternal;
			if(complaint.length()>0)
			{	
				String complaintDescription = complaint+" TAKEN-BY:"+UserNut.getUserName(iComplaintTakenBy)+" AT:"+complaintTime;
				SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(lOrder);
				soObj.customerComplaints = StringManup.truncate(complaintDescription,1000);
				soObj.flagInternal = flagInternal;
				soEJB.setObject(soObj);

				DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
				dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
				dpiObj.processType = "UPDATE-ORDER";
				dpiObj.category = "ORDER-RECORD-COMPLAINT";
				dpiObj.auditLevel = new Integer(0);
				dpiObj.processId = new Long(0);
				dpiObj.userid = userId;
				dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
				dpiObj.docId = soObj.pkid;
				dpiObj.entityRef = CustAccountBean.TABLENAME;
				dpiObj.entityId = soObj.senderKey1;
				dpiObj.description1 = complaintDescription;
				dpiObj.description2 = "";
				dpiObj.remarks = "";
				dpiObj.time = TimeFormat.getTimestamp();
				DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);
				req.setAttribute("closeAndRefreshParent","true");

			}

			if(actionTaken.length()>0)
			{
				String actionDescription = actionTaken +" ACTION-BY"+UserNut.getUserName(iActionTakenBy)+" AT:"+actionTakenTime;

            DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
            dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
            dpiObj.processType = "UPDATE-ORDER";
            dpiObj.category = "ORDER-RECORD-COMPLAINT-ACTION";
            dpiObj.auditLevel = new Integer(0);
            dpiObj.processId = new Long(0);
            dpiObj.userid = userId;
            dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
            dpiObj.docId = soObj.pkid;
            dpiObj.entityRef = CustAccountBean.TABLENAME;
            dpiObj.entityId = soObj.senderKey1;
            dpiObj.description1 = actionDescription;
            dpiObj.description2 = "";
            dpiObj.remarks = "";
            dpiObj.time = TimeFormat.getTimestamp();
            DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);
				req.setAttribute("closeAndRefreshParent","true");
			}

		}
		catch(Exception ex)
		{ 
			req.setAttribute("errMsg",ex.getMessage());
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

public class DoRecordOrderComplaint extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		req.setAttribute("orderNo",req.getParameter("orderNo"));

		if(formName==null)
		{
			return new ActionRouter("dist-record-order-complaint-page");
		}

		if(formName.equals("saveComplaint"))
		{
			fnSaveComplaint(servlet,req,res);
		}

		return new ActionRouter("dist-record-order-complaint-page");
	}

	private void fnSaveComplaint(HttpServlet servlet, HttpServletRequest req,
											HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		String orderNo = req.getParameter("orderNo");
		String complaintTakenBy = req.getParameter("complaintTakenBy");
		String complaintTime = req.getParameter("complaintTime");
		String complaint = req.getParameter("complaint");
		String actionTakenBy = req.getParameter("actionTakenBy");
		String actionTakenTime = req.getParameter("actionTakenTime");
		String actionTaken = req.getParameter("actionTaken");
		String flagInternal = req.getParameter("flagInternal");

		complaint = complaint.trim();
		actionTaken = actionTaken.trim();

		try
		{
			Integer iComplaintTakenBy = new Integer(complaintTakenBy);
			Integer iActionTakenBy = new Integer(actionTakenBy);

			Long lOrder = new Long(orderNo);
			SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(lOrder);
			String oldFlagInternal = soObj.flagInternal;
			if(complaint.length()>0)
			{	
				String complaintDescription = complaint+" TAKEN-BY:"+UserNut.getUserName(iComplaintTakenBy)+" AT:"+complaintTime;
				SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(lOrder);
				soObj.customerComplaints = StringManup.truncate(complaintDescription,1000);
				soObj.flagInternal = flagInternal;
				soEJB.setObject(soObj);

				DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
				dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
				dpiObj.processType = "UPDATE-ORDER";
				dpiObj.category = "ORDER-RECORD-COMPLAINT";
				dpiObj.auditLevel = new Integer(0);
				dpiObj.processId = new Long(0);
				dpiObj.userid = userId;
				dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
				dpiObj.docId = soObj.pkid;
				dpiObj.entityRef = CustAccountBean.TABLENAME;
				dpiObj.entityId = soObj.senderKey1;
				dpiObj.description1 = complaintDescription;
				dpiObj.description2 = "";
				dpiObj.remarks = "";
				dpiObj.time = TimeFormat.getTimestamp();
				DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);
				req.setAttribute("closeAndRefreshParent","true");

			}

			if(actionTaken.length()>0)
			{
				String actionDescription = actionTaken +" ACTION-BY"+UserNut.getUserName(iActionTakenBy)+" AT:"+actionTakenTime;

            DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
            dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
            dpiObj.processType = "UPDATE-ORDER";
            dpiObj.category = "ORDER-RECORD-COMPLAINT-ACTION";
            dpiObj.auditLevel = new Integer(0);
            dpiObj.processId = new Long(0);
            dpiObj.userid = userId;
            dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
            dpiObj.docId = soObj.pkid;
            dpiObj.entityRef = CustAccountBean.TABLENAME;
            dpiObj.entityId = soObj.senderKey1;
            dpiObj.description1 = actionDescription;
            dpiObj.description2 = "";
            dpiObj.remarks = "";
            dpiObj.time = TimeFormat.getTimestamp();
            DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);
				req.setAttribute("closeAndRefreshParent","true");
			}

		}
		catch(Exception ex)
		{ 
			req.setAttribute("errMsg",ex.getMessage());
		}
	}

}



