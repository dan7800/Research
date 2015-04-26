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

import java.sql.*;
import javax.sql.*;
import java.math.*;

import com.vlee.ejb.user.*;
import com.vlee.util.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;
import com.vlee.bean.distribution.*;

public class DoEditOrderLockEngineListing extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if(formName==null)
		{
			return new ActionRouter("dist-edit-order-lock-engine-listing-page");
		}

		if(formName.equals("removeLock"))
		{
			fnRemoveLock(servlet,req,res);
		}

		return new ActionRouter("dist-edit-order-lock-engine-listing-page");
	}

	private void fnRemoveLock(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String removeKey = req.getParameter("removeKey");
		String soPkid = (String) req.getParameter("soPkid");
			
		System.out.println("soPkid :"+soPkid);
		
		try
	    {	       
	       {
		         System.out.println("Checkpoint 222");
		         
		         HttpSession session = req.getSession();
		         Integer userId = (Integer) session.getAttribute("userId");
		         
		         DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
		         dpiObj.processType = "ORDER-UNLOCK";
		         dpiObj.category = "UNLOCK-ORDER";
		         dpiObj.auditLevel = new Integer(0);
		         dpiObj.userid = userId;
		         dpiObj.docRef = "cust_sales_order_index";
		         dpiObj.docId = new Long(soPkid);
		         dpiObj.description1 = "Order is unlocked from edit lock engine.";
		         dpiObj.time = TimeFormat.getTimestamp();
		         dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
		         dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
		         DocumentProcessingItemNut.fnCreate(dpiObj);
						
		         System.out.println("Checkpoint 333");
			 }
	       
	       EditOrderLockEngine.removeLock(removeKey);
	       
	       System.out.println("Checkpoint 444");
	    }
	    catch(Exception ex)
	    {
	       ex.printStackTrace();
	    }
	}
}





