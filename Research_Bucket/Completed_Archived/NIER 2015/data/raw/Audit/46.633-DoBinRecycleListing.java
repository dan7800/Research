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

public class DoBinRecycleListing extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("dist-bin-recycle-listing-page");
		}

      if(formName.equals("removeFromRecycleBin"))
      {
         fnRemoveFromRecycleBin(servlet,req,res);
      }

		return new ActionRouter("dist-bin-recycle-listing-page");
	}


   private void fnRemoveFromRecycleBin(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
   {
      String dpiPkid = req.getParameter("dpiPkid");
      try
      {
         Long dpiId = new Long(dpiPkid);
         DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiId);
         
         {
	         DocumentProcessingItemObject dpiObj2 = dpiEJB.getObject();
	         
	         HttpSession session = req.getSession();
	         Integer userId = (Integer) session.getAttribute("userId");
	         
	         DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
	         dpiObj.processType = "ORDER-RECYCLE";
	         dpiObj.category = "TAKE-OUT-FROM-RECYCLE-BIN";
	         dpiObj.auditLevel = new Integer(0);
	         dpiObj.userid = userId;
	         dpiObj.docRef = dpiObj2.docRef;
	         dpiObj.docId = dpiObj2.docId;
	         dpiObj.description1 = "Order is taken out from recycle bin to re-use.";
	         dpiObj.time = TimeFormat.getTimestamp();
	         dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
	         dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
	         DocumentProcessingItemNut.fnCreate(dpiObj);
					
	         System.out.println("Order number :"+dpiObj2.docId);
		 }
         
         dpiEJB.remove();
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
      }
   }

}





