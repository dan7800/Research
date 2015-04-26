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

public class DoRecordUnfulfilledOrder extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		req.setAttribute("orderNo",req.getParameter("orderNo"));

		if(formName==null)
		{
			return new ActionRouter("dist-record-unfulfilled-order-page");
		}

		if(formName.equals("saveInfo"))
		{
			fnSaveInfo(servlet,req,res);
		}

		return new ActionRouter("dist-record-unfulfilled-order-page");
	}

	private void fnSaveInfo(HttpServlet servlet, HttpServletRequest req,
											HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		String orderNo = req.getParameter("orderNo");

		String returnAcceptedBy = req.getParameter("returnAcceptedBy");
		String returnDateTime = req.getParameter("returnDateTime");
		String returnReason = req.getParameter("returnReason");
		String itemCOL = req.getParameter("itemCOL");
		String allowNewTrip = req.getParameter("allowNewTrip");

		try
		{
			Integer iReturnAcceptedBy = new Integer(returnAcceptedBy);

			Long lOrder = new Long(orderNo);
			SalesOrderIndexObject soObj = SalesOrderIndexNut.getObjectTree(lOrder);

			if(returnReason.length()>0)
			{	
				String description = returnReason+"<br>TAKEN BY: "+UserNut.getUserName(iReturnAcceptedBy)+" ("+ TimeFormat.format(TimeFormat.createTimeStamp(returnDateTime), "yyyy-MM-dd HH:mm:ss") + ")";

				DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
				dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
				dpiObj.processType = "UPDATE-ORDER";
				dpiObj.category = "RECORD-UNFULFILLED-ORDER";
				dpiObj.auditLevel = new Integer(0);
				dpiObj.processId = new Long(0);
				dpiObj.userid = userId;
				dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
				dpiObj.docId = soObj.pkid;
				dpiObj.entityRef = CustAccountBean.TABLENAME;
				dpiObj.entityId = soObj.senderKey1;
				dpiObj.description1 = description;
				dpiObj.description2 = "ALLOW NEW TRIP: "+allowNewTrip;
				dpiObj.remarks = "COL: "+itemCOL;
				dpiObj.time = TimeFormat.getTimestamp();
				DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);
				req.setAttribute("closeAndRefreshParent","true");


				if(allowNewTrip.equals("YES"))
				{
					for (int cnt1 = 0; cnt1 < soObj.vecItem.size(); cnt1++)
					{
						SalesOrderItemObject soItmObj = (SalesOrderItemObject) soObj.vecItem.get(cnt1);
						SalesOrderItem soItmEJB = SalesOrderItemNut.getHandle(soItmObj.pkid);
						try
						{ soItmEJB.setDeliveryStatus(SalesOrderItemBean.DELIVERY_STATUS_NONE); }
						catch(Exception ex)
						{ ex.printStackTrace();}


						Vector vecTripLink = new Vector(DeliveryTripSOLinkNut.getObjectsBySalesOrderItem(soItmObj.pkid));
						for (int cnt2 = 0; cnt2 < vecTripLink.size(); cnt2++)
						{
							DeliveryTripSOLinkObject tripLinkObj = (DeliveryTripSOLinkObject) vecTripLink.get(cnt2);
							try
		               		{
		                  		DeliveryTripSOLink tripLinkEJB = DeliveryTripSOLinkNut.getHandle(tripLinkObj.pkid);
		                  		tripLinkEJB.remove();
		               		} catch (Exception ex)
		               		{
		                  		ex.printStackTrace();
		               		}
						}// / end for
					}
				}/// end if allowNewTrip == YES
				
				SalesOrderItem soItmEJB2 = SalesOrderItemNut.getHandle(soObj.pkid);
				if(soItmEJB2!=null)
				{
					SalesOrderItemObject soItmObj2 = soItmEJB2.getObject();
					soItmObj2.valueadd1Name = itemCOL;
					soItmEJB2.setObject(soItmObj2);
				}
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

public class DoRecordUnfulfilledOrder extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		req.setAttribute("orderNo",req.getParameter("orderNo"));

		if(formName==null)
		{
			return new ActionRouter("dist-record-unfulfilled-order-page");
		}

		if(formName.equals("saveInfo"))
		{
			fnSaveInfo(servlet,req,res);
		}

		return new ActionRouter("dist-record-unfulfilled-order-page");
	}

	private void fnSaveInfo(HttpServlet servlet, HttpServletRequest req,
											HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		String orderNo = req.getParameter("orderNo");

		String returnAcceptedBy = req.getParameter("returnAcceptedBy");
		String returnDateTime = req.getParameter("returnDateTime");
		String returnReason = req.getParameter("returnReason");
		String allowNewTrip = req.getParameter("allowNewTrip");

		try
		{
			Integer iReturnAcceptedBy = new Integer(returnAcceptedBy);

			Long lOrder = new Long(orderNo);
			SalesOrderIndexObject soObj = SalesOrderIndexNut.getObjectTree(lOrder);

			if(returnReason.length()>0)
			{	
				String description = returnReason+" TAKEN-BY:"+UserNut.getUserName(iReturnAcceptedBy)+" AT:"+returnDateTime;

				DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
				dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
				dpiObj.processType = "UPDATE-ORDER";
				dpiObj.category = "RECORD-UNFULFILLED-ORDER";
				dpiObj.auditLevel = new Integer(0);
				dpiObj.processId = new Long(0);
				dpiObj.userid = userId;
				dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
				dpiObj.docId = soObj.pkid;
				dpiObj.entityRef = CustAccountBean.TABLENAME;
				dpiObj.entityId = soObj.senderKey1;
				dpiObj.description1 = description;
				dpiObj.description2 = "ALLOW-NEW-TRIP:"+allowNewTrip;
				dpiObj.remarks = "";
				dpiObj.time = TimeFormat.getTimestamp();
				DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);
				req.setAttribute("closeAndRefreshParent","true");


				if(allowNewTrip.equals("YES"))
				{
					for (int cnt1 = 0; cnt1 < soObj.vecItem.size(); cnt1++)
					{
						SalesOrderItemObject soItmObj = (SalesOrderItemObject) soObj.vecItem.get(cnt1);
						SalesOrderItem soItmEJB = SalesOrderItemNut.getHandle(soItmObj.pkid);
						try
						{ soItmEJB.setDeliveryStatus(SalesOrderItemBean.DELIVERY_STATUS_NONE); }
						catch(Exception ex)
						{ ex.printStackTrace();}


						Vector vecTripLink = new Vector(DeliveryTripSOLinkNut.getObjectsBySalesOrderItem(soItmObj.pkid));
						for (int cnt2 = 0; cnt2 < vecTripLink.size(); cnt2++)
						{
							DeliveryTripSOLinkObject tripLinkObj = (DeliveryTripSOLinkObject) vecTripLink.get(cnt2);
							try
               		{
                  		DeliveryTripSOLink tripLinkEJB = DeliveryTripSOLinkNut.getHandle(tripLinkObj.pkid);
                  		tripLinkEJB.remove();
               		} catch (Exception ex)
               		{
                  		ex.printStackTrace();
               		}
            		}// / end for
					}
				}/// end if allowNewTrip == YES
			}

		}
		catch(Exception ex)
		{ 
			req.setAttribute("errMsg",ex.getMessage());
		}
	}

}



