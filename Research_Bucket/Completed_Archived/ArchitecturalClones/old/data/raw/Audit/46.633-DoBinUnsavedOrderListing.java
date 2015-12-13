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
import java.util.*;

import com.vlee.ejb.user.*;
import com.vlee.util.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;
import com.vlee.bean.distribution.*;

public class DoBinUnsavedOrderListing extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("dist-bin-unsaved-order-listing-page");
		}

		if(formName.equals("recycleOrder"))
		{
			fnRecycleOrder(servlet,req,res);
		}

		if(formName.equals("removeFromUnsavedList"))
		{
			fnRemoveFromUnsavedList(servlet,req,res);
		}

		return new ActionRouter("dist-bin-unsaved-order-listing-page");
	}

	private void fnRecycleOrder(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		String[] orderId = req.getParameterValues("orderId");
		if(orderId==null || orderId.length ==0)
		{ return ;}

		for(int cnt1=0;cnt1<orderId.length;cnt1++)
		{
			try
			{
				Long orderPkid  = new Long(orderId[cnt1]);
				Integer userId = (Integer) session.getAttribute("userId");
				//// create a row in the recycle bin
				{
					DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
					dpiObj.processType = "ORDER-RECYCLE";
					dpiObj.category = "RECYCLE-BIN";
					dpiObj.auditLevel = new Integer(0);
		//       dpiObj.processId = new Long(0);
					dpiObj.userid = userId;
					dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
					dpiObj.docId = orderPkid;
		//       dpiObj.entityRef = "";
		//       dpiObj.entityId = new Integer(0);
					dpiObj.description1 = "THIS ORDER IS IN RECYCLE BIN";
		//       dpiObj.description2 = "";
		//       dpiObj.remarks = "";
					dpiObj.time = TimeFormat.getTimestamp();
					dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
					dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
					DocumentProcessingItemNut.fnCreate(dpiObj);
				}

				////  create a log history for this action
				{
					DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
					dpiObj.processType = "ORDER-RECYCLE";
					dpiObj.category = "PUT-INSIDE-RECYCLE-BIN";
					dpiObj.auditLevel = new Integer(0);
		//       dpiObj.processId = new Long(0);
					dpiObj.userid = userId;
					dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
					dpiObj.docId = orderPkid;
		//       dpiObj.entityRef = "";
		//       dpiObj.entityId = new Integer(0);
					dpiObj.description1 = "Order is placed inside recycle bin for re-use from unsaved order bin.";
		//       dpiObj.description2 = "";
		//       dpiObj.remarks = "";
					dpiObj.time = TimeFormat.getTimestamp();
		         dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
					dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
		         DocumentProcessingItemNut.fnCreate(dpiObj);
				}

				//// closing the edit form
				EditSalesOrderSession esos = new EditSalesOrderSession(userId);
				esos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
				esos.loadSalesOrder(orderPkid);
				esos.setMode(EditSalesOrderSession.MODE_CREATE);
				esos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
				esos.setReceiptMode("");
				CustAccountObject custObj = CustAccountNut.getObject(CustAccountBean.PKID_CASH, "");
				esos.setCustomer(custObj);
				esos.setDeliveryDetails("", "", "" , "", "" , "", "0001-01-01",//7
							"", "", "", "", "" ,//12
							"" , "" , "" , "", //16
							"" , "" , "" , "", "", //21
							"", "", "", "", "",//26
							"", "", "", "", "",//31
							"", "", "", "",//35
							"", "", "", "", "", true, "");
				esos.dropAllDocRow();


				/// MUST REMOVE THIS ORDER FROM THE UNSAVE-ORDER-BIN
				QueryObject queryUnsavedOrder = new QueryObject(new String[]{
					DocumentProcessingItemBean.DOC_ID +" = '"+orderPkid.toString()+"' ",
					DocumentProcessingItemBean.CATEGORY +" = 'UNSAVED-ORDER-BIN' ",
					DocumentProcessingItemBean.PROCESS_TYPE + " = 'ORDER-CREATION' ",
					DocumentProcessingItemBean.DOC_REF+" ='"+SalesOrderIndexBean.TABLENAME+"' "
                     });
				queryUnsavedOrder.setOrder(" ORDER BY "+DocumentProcessingItemBean.DOC_ID ) ;

				Vector vecUnsavedOrder = new Vector(DocumentProcessingItemNut.getObjects(queryUnsavedOrder));
				for(int cnt2=0;cnt2<vecUnsavedOrder.size();cnt2++)
				{
					DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) vecUnsavedOrder.get(cnt2);
					try
					{
						DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiObj.pkid);
						dpiEJB.remove();
					}
					catch(Exception ex)
					{ ex.printStackTrace();}	

				}
				
			}
			catch(Exception ex)
			{ ex.printStackTrace();}
		}
	}

	private void fnRemoveFromUnsavedList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String dpiPkid=req.getParameter("dpiPkid");
		try
		{
			Long dpiId = new Long(dpiPkid);
			DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiId);
			dpiEJB.remove();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

}

