/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.user;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.BranchNut;
import com.vlee.ejb.accounting.BranchObject;
import com.vlee.ejb.customer.*;

// public class DoEditUserSettings extends ActionDo implements Action
public class DoEditUserSettings implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		HttpSession session = req.getSession(true);
		String fwdPage = req.getParameter("fwdPage");
		String userName = req.getParameter("userName");
		String formName = req.getParameter("formName");
		if (fwdPage == null)
		{
			fwdPage = "user-edit-user-settings-page";
		}
		if (userName == null)
		{
			userName = (String) session.getAttribute("userName");
		}
		req.setAttribute("userName", userName);
		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		if (formName.equals("setDefaultCustSvcCtr"))
		{
			fnSetCustSvcCtr(servlet, req, res);
			return new ActionRouter(fwdPage);
		}
		if (formName.equals("setDefaultProcurementCtr"))
		{
			fnSetProcCtr(servlet, req, res);
			return new ActionRouter(fwdPage);
		}
		return new ActionRouter(fwdPage);
	}// perform

	protected void fnSetCustSvcCtr(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String userName = req.getParameter("userName");
		Integer userId = UserNut.getUserId(userName);
		UserConfigObject ucObj = new UserConfigObject();
		if (userId != null)
		{
			ucObj = UserConfigRegistryNut.getUserConfigObject(userId);
			
			String custSvcCtr = req.getParameter("custSvcCtr");
			Log.printVerbose("New custSvcCtr is " + custSvcCtr);
			Integer iSvcCtr = new Integer(custSvcCtr);
			
			BranchObject originalBranchObj = BranchNut.getObject(ucObj.mDefaultCustSvcCtr);
			BranchObject newBranchObj = BranchNut.getObject(iSvcCtr);
			
			if (ucObj.mDefaultCustSvcCtr != null)
			{
				/// if property exist in db, reset the value
				ucObj.mDefaultCustSvcCtr = iSvcCtr;
				UserConfigRegistryNut.setUserConfigObject(ucObj);
				HttpSession session = req.getSession(true);
				session.setAttribute("ucObj", ucObj);
				
				{
					System.out.println("Property exist in db");
					System.out.println("Recording change of user config to audit trail, user : "+userId.toString());
					
					AuditTrailObject atObj = new AuditTrailObject();
					atObj.userId = (Integer) session.getAttribute("userId");
					atObj.auditType = AuditTrailBean.TYPE_CONFIG;
					atObj.time = TimeFormat.getTimestamp();
					atObj.remarks = "change-default-branch : User " + userName + " (" + originalBranchObj.description + " --> " + newBranchObj.description + ")";
					AuditTrailNut.fnCreate(atObj);
					
					System.out.println("Recorded change of user config into audit trail");
				}
			} 
			else
			{
				/// if property does not exist, create a new one
				HttpSession session = req.getSession(true);
				Integer userIdEdit = (Integer) session.getAttribute("userId");
				UserConfigRegistryNut.fnCreate(userId, UserConfigRegistryBean.CAT_DEFAULT,
						UserConfigRegistryBean.NS_CUSTSVCCTR, custSvcCtr, (String) null, (String) null, (String) null,
						(String) null, userId, TimeFormat.getTimestamp(), TimeFormat.getTimestamp());
				ucObj = UserConfigRegistryNut.getUserConfigObject(userId);
				session.setAttribute("ucObj", ucObj);
				
				{
					System.out.println("Property not exist in db");
					System.out.println("Recording change of user config to audit trail, user : "+userId.toString());
					
					AuditTrailObject atObj = new AuditTrailObject();
					atObj.userId = (Integer) session.getAttribute("userId");
					atObj.auditType = AuditTrailBean.TYPE_CONFIG;
					atObj.time = TimeFormat.getTimestamp();
					atObj.remarks = "change-default-branch : User " + userName + " (" + originalBranchObj.description + " --> " + newBranchObj.description + ")";
					AuditTrailNut.fnCreate(atObj);
					
					System.out.println("Recorded change of user config into audit trail");
				}
			}
		}
		if (userId != null)
		{
			ucObj = UserConfigRegistryNut.getUserConfigObject(userId);
			String suppProcCtr = req.getParameter("custSvcCtr");
			Log.printVerbose("suppProcCtr selected is " + suppProcCtr);
			Integer iProcCtr = new Integer(suppProcCtr);
			if (ucObj.mDefaultSuppProcCtr != null)
			{
				// / if property exist in db, reset the value
				ucObj.mDefaultSuppProcCtr = iProcCtr;
				UserConfigRegistryNut.setUserConfigObject(ucObj);
				HttpSession session = req.getSession(true);
				session.setAttribute("ucObj", ucObj);
			} else
			{
				// / if property does not exist, create a new one
				HttpSession session = req.getSession(true);
				Integer userIdEdit = (Integer) session.getAttribute("userId");
				UserConfigRegistryNut.fnCreate(userId, UserConfigRegistryBean.CAT_DEFAULT,
						UserConfigRegistryBean.NS_PROCCTR, suppProcCtr, (String) null, (String) null, (String) null,
						(String) null, userId, TimeFormat.getTimestamp(), TimeFormat.getTimestamp());
				ucObj = UserConfigRegistryNut.getUserConfigObject(userId);
				session.setAttribute("ucObj", ucObj);
			}
		}
	}

	protected void fnSetProcCtr(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String userName = req.getParameter("userName");
		Integer userId = UserNut.getUserId(userName);
		UserConfigObject ucObj = new UserConfigObject();
		if (userId != null)
		{
			ucObj = UserConfigRegistryNut.getUserConfigObject(userId);
			String suppProcCtr = req.getParameter("suppProcCtr");
			Log.printVerbose("suppProcCtr selected is " + suppProcCtr);
			Integer iProcCtr = new Integer(suppProcCtr);
			if (ucObj.mDefaultSuppProcCtr != null)
			{
				// / if property exist in db, reset the value
				ucObj.mDefaultSuppProcCtr = iProcCtr;
				UserConfigRegistryNut.setUserConfigObject(ucObj);
				HttpSession session = req.getSession(true);
				session.setAttribute("ucObj", ucObj);
			} else
			{
				// / if property does not exist, create a new one
				HttpSession session = req.getSession(true);
				Integer userIdEdit = (Integer) session.getAttribute("userId");
				UserConfigRegistryNut.fnCreate(userId, UserConfigRegistryBean.CAT_DEFAULT,
						UserConfigRegistryBean.NS_PROCCTR, suppProcCtr, (String) null, (String) null, (String) null,
						(String) null, userId, TimeFormat.getTimestamp(), TimeFormat.getTimestamp());
				ucObj = UserConfigRegistryNut.getUserConfigObject(userId);
				session.setAttribute("ucObj", ucObj);
			}
		}
	}
}
